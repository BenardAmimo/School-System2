package com.school.money.service;

import com.school.money.Access;
import com.school.money.MoneyException;
import com.school.money.MoneyProperties;
import com.school.money.config.MoneyNotifier;
import com.school.money.entity.AdvanceStatus;
import com.school.money.entity.PayeeType;
import com.school.money.entity.Purpose;
import com.school.money.entity.moneyEntities.PaymentOrder;
import com.school.money.entity.moneyEntities.SalaryAdvance;
import com.school.money.entity.moneyEntities.StaffPayProfile;
import com.school.money.repository.SalaryAdvanceRepository;
import com.school.money.request.AdvanceRequest;
import com.school.money.request.OrderSettledEvent;
import com.school.money.request.ReasonRequest;
import com.school.money.request.RepaymentRequest;
import com.school.money.response.AdvanceResponse;
import com.school.money.response.AdvanceSummaryResponse;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Salary advances. Limit: 3/4 of one month's net salary. Repay once or in up to 4 monthly instalments,
 * taken from payroll. One open advance per person.
 */
@Service
public class AdvanceService {

    private final SalaryAdvanceRepository repo;
    private final PayrollService payroll;
    private final PaymentOrderService orders;
    private final MoneyProperties props;
    private final MoneyNotifier notifier;

    public AdvanceService(SalaryAdvanceRepository repo, PayrollService payroll, PaymentOrderService orders, MoneyProperties props,
                          MoneyNotifier notifier) {
        this.repo = repo; this.payroll = payroll; this.orders = orders; this.props = props; this.notifier = notifier;
    }

    // ---- Entry points for the controller: who may do it, the work, and the mapping to the response ----

    /** Every staff member can request; refused when there is no salary on record. */
    @Transactional
    public AdvanceResponse request(AdvanceRequest r, Authentication auth) {
        return AdvanceResponse.from(request(Access.user(auth), r.amount(), r.instalments(), r.reason()));
    }

    @Transactional(readOnly = true)
    public AdvanceSummaryResponse mine(Authentication auth) {
        String u = Access.user(auth);
        return new AdvanceSummaryResponse(limitFor(u), AdvanceResponse.list(mine(u)));
    }

    @Transactional
    public AdvanceResponse cancel(Long id, Authentication auth) {
        return AdvanceResponse.from(cancel(id, Access.user(auth)));
    }

    @Transactional(readOnly = true)
    public List<AdvanceResponse> all(Authentication auth) {
        Access.preparer(auth);
        return AdvanceResponse.list(all());
    }

    @Transactional
    public AdvanceResponse approve(Long id, Authentication auth) {
        return AdvanceResponse.from(approve(id, Access.approver(auth)));
    }

    @Transactional
    public AdvanceResponse reject(Long id, ReasonRequest r, Authentication auth) {
        return AdvanceResponse.from(reject(id, r.reason(), Access.approver(auth)));
    }

    /** Repayment made outside payroll (cash, bank, M-Pesa). */
    @Transactional
    public AdvanceResponse recordRepayment(Long id, RepaymentRequest r, Authentication auth) {
        return AdvanceResponse.from(recordRepayment(id, r.amount(), r.reference(), Access.preparer(auth)));
    }

    // ---- The work itself ----

    /** Largest advance for this salary, in whole shillings. */
    public static BigDecimal maxAllowed(BigDecimal netSalary, BigDecimal fraction) {
        return netSalary.multiply(fraction).setScale(0, RoundingMode.DOWN);
    }

    /** Instalment rounded UP to whole shillings; the last payroll takes only what is left. */
    public static BigDecimal instalment(BigDecimal amount, int n) {
        return amount.divide(BigDecimal.valueOf(n), 0, RoundingMode.CEILING);
    }

    @Transactional
    public SalaryAdvance request(String username, BigDecimal amount, int instalments, String reason) {
        StaffPayProfile p = payroll.currentProfile(username, LocalDate.now())
                .filter(x -> x.getNetSalary().signum() > 0)
                .orElseThrow(() -> MoneyException.conflict("You have no salary on record, so you cannot request an advance."));
        int maxN = props.getAdvance().getMaxInstalments();
        if (instalments < 1 || instalments > maxN) throw MoneyException.bad("Repayment can be 1 to " + maxN + " months.");
        if (amount == null || amount.signum() <= 0) throw MoneyException.bad("Amount must be above zero.");
        if (amount.stripTrailingZeros().scale() > 0) throw MoneyException.bad("Enter whole shillings.");
        BigDecimal max = maxAllowed(p.getNetSalary(), props.getAdvance().getMaxSalaryFraction());
        if (amount.compareTo(max) > 0) throw MoneyException.bad("You can borrow at most " + max.toPlainString()
                + " (" + props.getAdvance().getMaxSalaryFraction().multiply(BigDecimal.valueOf(100)).stripTrailingZeros().toPlainString()
                + "% of one month's net salary).");
        if (repo.existsByStaffUsernameAndStatusIn(username, Set.of(AdvanceStatus.REQUESTED, AdvanceStatus.APPROVED, AdvanceStatus.ACTIVE))) {
            throw MoneyException.conflict("You already have an advance that is open. Finish repaying it first.");
        }
        SalaryAdvance a = new SalaryAdvance();
        a.setStaffUsername(username);
        a.setStaffName(p.getStaffName());
        a.setAmount(amount);
        a.setInstalments(instalments);
        a.setInstalmentAmount(instalment(amount, instalments));
        a.setSalaryAtRequest(p.getNetSalary());
        a.setReason(reason);
        a.setRequestedAt(LocalDateTime.now());
        repo.save(a);
        if (notifier != null) notifier.toRoles("Advance request from " + a.getStaffName(),
                a.getStaffName() + " asks for an advance of " + amount + " over " + instalments + " month(s).\nReason: " + reason
                        + "\nOpen Money out > Advances to approve or reject.", "ADMIN", "SUPER_ADMIN");
        return a;
    }

    /** Admin approves: creates the payment order. Cannot approve your own request. */
    @Transactional
    public SalaryAdvance approve(Long id, String actor) {
        SalaryAdvance a = get(id);
        if (a.getStatus() != AdvanceStatus.REQUESTED) throw MoneyException.conflict("Advance is " + a.getStatus() + ".");
        if (actor.equalsIgnoreCase(a.getStaffUsername())) throw MoneyException.forbidden("You cannot approve your own advance.");
        // Create the order first: if the payee details are missing or unverified this throws and nothing changes.
        orders.create(Purpose.ADVANCE, "ADVANCE", a.getId(), a.getAmount(), PayeeType.STAFF,
                a.getStaffUsername(), "Salary advance", actor);
        a.setStatus(AdvanceStatus.APPROVED);
        a.setDecidedBy(actor);
        a.setDecidedAt(LocalDateTime.now());
        repo.save(a);
        if (notifier != null) notifier.toUser(a.getStaffUsername(), "Your advance was approved",
                "Your advance of " + a.getAmount() + " was approved and will be paid shortly. It will be taken back as "
                        + a.getInstalments() + " monthly deduction(s) of about " + a.getInstalmentAmount() + " from your pay.");
        return a;
    }

    @Transactional
    public SalaryAdvance reject(Long id, String reason, String actor) {
        SalaryAdvance a = get(id);
        if (a.getStatus() != AdvanceStatus.REQUESTED) throw MoneyException.conflict("Advance is " + a.getStatus() + ".");
        if (reason == null || reason.isBlank()) throw MoneyException.bad("Give a reason.");
        a.setStatus(AdvanceStatus.REJECTED);
        a.setRejectReason(reason.trim());
        a.setDecidedBy(actor);
        a.setDecidedAt(LocalDateTime.now());
        repo.save(a);
        if (notifier != null) notifier.toUser(a.getStaffUsername(), "Your advance request was not approved",
                "Your request for " + a.getAmount() + " was not approved.\nReason: " + a.getRejectReason());
        return a;
    }

    /**
     * A repayment made outside payroll (cash, bank or M-Pesa paid in by the staff member, or the final balance when
     * someone leaves). Reduces what is owed; closes the advance when nothing is left. A reference is required.
     */
    @Transactional
    public SalaryAdvance recordRepayment(Long id, BigDecimal amount, String reference, String actor) {
        SalaryAdvance a = get(id);
        if (a.getStatus() != AdvanceStatus.ACTIVE) throw MoneyException.conflict("Only an ACTIVE advance can be repaid. This one is " + a.getStatus() + ".");
        if (amount == null || amount.signum() <= 0) throw MoneyException.bad("Amount must be above zero.");
        if (reference == null || reference.isBlank()) throw MoneyException.bad("Enter the receipt, M-Pesa or bank reference of the repayment.");
        if (amount.compareTo(a.getOutstanding()) > 0) {
            throw MoneyException.bad("The staff member owes " + a.getOutstanding() + ", which is less than " + amount + ".");
        }
        if (payroll.hasOpenRecovery(a.getId())) {
            throw MoneyException.conflict("An unfinished payroll already takes an instalment of this advance. "
                    + "Finish or delete that payroll first, so the money is not taken twice.");
        }
        a.setRepaid(a.getRepaid().add(amount));
        a.setRepaymentNotes(((a.getRepaymentNotes() == null ? "" : a.getRepaymentNotes() + "; ")
                + amount + " ref " + reference.trim() + " by " + actor).trim());
        if (a.getOutstanding().signum() <= 0) a.setStatus(AdvanceStatus.CLOSED);
        return repo.save(a);
    }

    /** Staff may withdraw while nothing has been decided. */
    @Transactional
    public SalaryAdvance cancel(Long id, String username) {
        SalaryAdvance a = get(id);
        if (!a.getStaffUsername().equalsIgnoreCase(username)) throw MoneyException.forbidden("Not your advance.");
        if (a.getStatus() != AdvanceStatus.REQUESTED) throw MoneyException.conflict("Advance is " + a.getStatus() + ".");
        a.setStatus(AdvanceStatus.REJECTED);
        a.setRejectReason("Withdrawn by staff");
        return repo.save(a);
    }

    /** Money reached the staff member: recovery starts with the next payroll. */
    @EventListener
    @Transactional
    public void onSettled(OrderSettledEvent ev) {
        PaymentOrder o = ev.order();
        if (o.getPurpose() != Purpose.ADVANCE) return;
        repo.findById(o.getSourceId()).ifPresent(a -> {
            if (a.getStatus() == AdvanceStatus.APPROVED) { a.setStatus(AdvanceStatus.ACTIVE); repo.save(a); }
        });
    }

    @Transactional(readOnly = true)
    public List<SalaryAdvance> all() { return repo.findAllByOrderByIdDesc(); }

    @Transactional(readOnly = true)
    public List<SalaryAdvance> mine(String u) { return repo.findByStaffUsernameOrderByIdDesc(u); }

    /** What this person may ask for right now. */
    @Transactional(readOnly = true)
    public BigDecimal limitFor(String username) {
        return payroll.currentProfile(username, LocalDate.now())
                .map(p -> maxAllowed(p.getNetSalary(), props.getAdvance().getMaxSalaryFraction())).orElse(BigDecimal.ZERO);
    }

    private SalaryAdvance get(Long id) { return repo.findById(id).orElseThrow(() -> MoneyException.notFound("Advance not found.")); }
}