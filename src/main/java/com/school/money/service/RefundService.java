package com.school.money.service;

import com.school.money.Access;
import com.school.money.MoneyException;
import com.school.money.MoneyProperties;
import com.school.money.config.MoneyNotifier;
import com.school.money.entity.*;
import com.school.money.entity.moneyEntities.PaymentOrder;
import com.school.money.port.StudentLedgerPort;
import com.school.money.repository.FeeCreditRepository;
import com.school.money.repository.PaymentOrderRepository;
import com.school.money.repository.RefundCreditRepository;
import com.school.money.request.*;
import com.school.money.response.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Overpaid fees.
 *   1. scan(term) finds students who paid more than due and opens one credit each (never two for the same student and term)
 *   2. the PARENT chooses: REFUND (back to the number(s) that paid, never more than each paid) or CARRY_FORWARD
 *   3. if the parent does not answer within N days, CARRY_FORWARD is chosen for them (autoDecided = true)
 *   4. an Admin approves either way. Nothing moves before that.
 */
@Service
public class RefundService {

    private static final Logger log = LoggerFactory.getLogger(RefundService.class);

    /** One part of a refund, going to one payer. */
    public record Part(String phone, BigDecimal amount) {}

    private final RefundCreditRepository credits;
    private final FeeCreditRepository feeCredits;
    private final StudentLedgerPort ledger;
    private final PaymentOrderService orders;
    private final PaymentOrderRepository orderRepo;
    private final MoneyProperties props;
    private final MoneyNotifier notifier;

    public RefundService(RefundCreditRepository credits, FeeCreditRepository feeCredits, StudentLedgerPort ledger,
                         PaymentOrderService orders, PaymentOrderRepository orderRepo, MoneyProperties props,
                         MoneyNotifier notifier) {
        this.credits = credits; this.feeCredits = feeCredits; this.ledger = ledger;
        this.orders = orders; this.orderRepo = orderRepo; this.props = props; this.notifier = notifier;
    }

    // =====================================================================
    //  Entry points for the controller: who may do it, the work, the response
    // =====================================================================

    /** Finds overpaid students for a term and opens a credit for each. */
    @Transactional
    public ScanResponse scan(ScanRequest r, Authentication auth) {
        Access.preparer(auth);
        if (r == null || r.termId() == null) throw MoneyException.bad("termId is required.");
        return new ScanResponse(scan(r.termId()));
    }

    @Transactional(readOnly = true)
    public List<RefundResponse> all(Authentication auth) {
        Access.preparer(auth);
        return RefundResponse.list(all());
    }

    /** The parent sees only the credits of their own children. */
    @Transactional(readOnly = true)
    public List<RefundResponse> mine(Authentication auth) {
        return RefundResponse.list(mine(Access.user(auth)));
    }

    @Transactional
    public RefundResponse choose(Long id, RefundChoiceRequest r, Authentication auth) {
        RefundChoice choice;
        try { choice = RefundChoice.valueOf(r.choice()); } catch (Exception e) { throw MoneyException.bad("Choice must be REFUND or CARRY_FORWARD."); }
        return RefundResponse.from(choose(id, choice, Access.user(auth)));
    }

    @Transactional
    public RefundResponse approve(Long id, Authentication auth) {
        return RefundResponse.from(approve(id, Access.approver(auth)));
    }

    @Transactional
    public RefundResponse reject(Long id, ReasonRequest r, Authentication auth) {
        return RefundResponse.from(reject(id, r.reason(), Access.approver(auth)));
    }

    // =====================================================================
    //  The work itself
    // =====================================================================

    /**
     * Splits an amount over the payers, most recent first, never more than each paid.
     * Throws if the payers together paid less than the amount.
     */
    public static List<Part> allocate(BigDecimal amount, List<StudentLedgerPort.Payer> payers) {
        List<Part> out = new ArrayList<>();
        BigDecimal left = amount;
        for (StudentLedgerPort.Payer p : payers) {
            if (left.signum() <= 0) break;
            BigDecimal take = left.min(p.paid().setScale(0, RoundingMode.DOWN));
            if (take.signum() > 0) { out.add(new Part(p.phone(), take)); left = left.subtract(take); }
        }
        if (left.signum() > 0) {
            throw MoneyException.conflict("The payers on record paid " + amount.subtract(left)
                    + " in total, which is less than the credit " + amount + ". Use carry-forward or fix the payment records.");
        }
        return out;
    }

    // ---------- 1. find credits ----------

    @Transactional
    public int scan(Long termId) {
        int created = 0;
        for (Long sid : ledger.studentsWithPayments(termId)) {
            BigDecimal credit = ledger.creditFor(sid, termId).setScale(0, RoundingMode.DOWN);
            if (credit.signum() <= 0) continue;
            RefundCredit rc = credits.findByStudentIdAndTermId(sid, termId).orElse(null);
            if (rc == null) {
                rc = new RefundCredit();
                rc.setStudentId(sid);
                rc.setStudentName(ledger.studentName(sid));
                rc.setTermId(termId);
                rc.setAmount(credit);
                rc.setCreatedAt(LocalDateTime.now());
                rc.setDecisionDeadline(LocalDateTime.now().plusDays(props.getRefund().getDecisionDays()));
                credits.save(rc);
                created++;
                tell(rc, "Overpayment on " + rc.getStudentName() + "'s fees",
                        "We received " + rc.getAmount() + " more than the fees for " + rc.getStudentName() + ".\n"
                                + "Please sign in and choose: a refund to the M-Pesa number that paid, or use it for next term's fees.\n"
                                + "If you do not choose by " + rc.getDecisionDeadline().toLocalDate() + ", it will be carried forward to next term automatically.");
            } else if (rc.getStatus() == RefundStatus.AWAITING_PARENT && rc.getAmount().compareTo(credit) != 0) {
                rc.setAmount(credit);     // still undecided: keep it in line with the ledger
                credits.save(rc);
            }
        }
        return created;
    }

    /** Every night: look for overpayments in the two most recent terms and tell the parents. */
    @Scheduled(cron = "0 0 3 * * *")
    public void autoScan() {
        if (!props.getRefund().isAutoScan()) return;
        for (Long termId : ledger.recentTermIds()) {
            try {
                int n = scan(termId);
                if (n > 0) log.info("Refund scan of term {} opened {} credit(s)", termId, n);
            } catch (RuntimeException e) {
                log.warn("Refund scan of term {} failed: {}", termId, e.getMessage());
            }
        }
    }

    private void tell(RefundCredit rc, String subject, String body) {
        if (notifier == null) return;
        try {
            notifier.toUser(ledger.parentEmailOfStudent(rc.getStudentId()), subject, body);
        } catch (RuntimeException e) {
            log.warn("Could not find the parent to notify for credit {}: {}", rc.getId(), e.getMessage());
        }
    }

    // ---------- 2. parent chooses ----------

    @Transactional
    public RefundCredit choose(Long id, RefundChoice choice, String parentUsername) {
        RefundCredit rc = get(id);
        if (!ledger.studentIdsOfParent(parentUsername).contains(rc.getStudentId())) {
            throw MoneyException.forbidden("This credit does not belong to one of your children.");
        }
        if (rc.getStatus() != RefundStatus.AWAITING_PARENT) throw MoneyException.conflict("A choice was already made.");
        if (choice == null) throw MoneyException.bad("Choose REFUND or CARRY_FORWARD.");
        rc.setChoice(choice);
        rc.setDecidedBy(parentUsername);
        rc.setDecidedAt(LocalDateTime.now());
        rc.setStatus(RefundStatus.AWAITING_APPROVAL);
        credits.save(rc);
        if (notifier != null) notifier.toRoles("Refund choice waiting for approval",
                "The parent of " + rc.getStudentName() + " chose " + choice.name().replace('_', ' ').toLowerCase() + " for " + rc.getAmount()
                        + ".\nOpen Money out > Refunds to approve.", "ADMIN", "SUPER_ADMIN");
        return rc;
    }

    // ---------- 3. default after the deadline ----------

    @Scheduled(cron = "0 15 2 * * *")
    @Transactional
    public void applyDefaults() {
        for (RefundCredit rc : credits.findByStatusAndDecisionDeadlineBefore(RefundStatus.AWAITING_PARENT, LocalDateTime.now())) {
            rc.setChoice(RefundChoice.CARRY_FORWARD);
            rc.setAutoDecided(true);
            rc.setDecidedBy("SYSTEM");
            rc.setDecidedAt(LocalDateTime.now());
            rc.setStatus(RefundStatus.AWAITING_APPROVAL);
            credits.save(rc);
            log.info("Refund credit {} defaulted to carry-forward (parent did not answer)", rc.getId());
            tell(rc, "Overpayment carried forward for " + rc.getStudentName(),
                    "You did not choose by the deadline, so " + rc.getAmount() + " will be used for " + rc.getStudentName()
                            + "'s fees next term, once approved by the school.");
        }
    }

    // ---------- 4. admin approves ----------

    @Transactional
    public RefundCredit approve(Long id, String admin) {
        RefundCredit rc = get(id);
        if (rc.getStatus() != RefundStatus.AWAITING_APPROVAL) throw MoneyException.conflict("Credit is " + rc.getStatus() + ".");
        rc.setApprovedBy(admin);
        rc.setApprovedAt(LocalDateTime.now());

        if (rc.getChoice() == RefundChoice.CARRY_FORWARD) {
            Long next = ledger.nextTermId(rc.getTermId());
            if (next == null) throw MoneyException.conflict("Create next term first: there is no term to carry the credit into.");
            FeeCredit f = new FeeCredit();
            f.setStudentId(rc.getStudentId());
            f.setTargetTermId(next);
            f.setRefundCreditId(rc.getId());
            f.setAmount(rc.getAmount());
            f.setCreatedAt(LocalDateTime.now());
            feeCredits.save(f);
            rc.setStatus(RefundStatus.COMPLETED);
            tell(rc, "Overpayment carried forward for " + rc.getStudentName(),
                    rc.getAmount() + " has been taken off " + rc.getStudentName() + "'s fees for next term.");
        } else {
            // Re-check: the credit must still exist at approval time.
            BigDecimal now = ledger.creditFor(rc.getStudentId(), rc.getTermId()).setScale(0, RoundingMode.DOWN);
            if (now.compareTo(rc.getAmount()) < 0) {
                throw MoneyException.conflict("The student's balance changed: the credit is now " + now + ", not " + rc.getAmount() + ". Run the scan again.");
            }
            List<Part> parts = allocate(rc.getAmount(), ledger.payersFor(rc.getStudentId(), rc.getTermId()));
            rc.setStatus(RefundStatus.PAYING);
            credits.saveAndFlush(rc);
            int i = 1;
            for (Part p : parts) {
                orders.createToPhone(Purpose.REFUND, "REFUND_PART", rc.getId() * 100 + i, p.amount(),
                        "Refund " + rc.getStudentName(), p.phone(), "Fee refund " + rc.getStudentName(), admin);
                i++;
            }
            tell(rc, "Your refund was approved", "A refund of " + rc.getAmount() + " for " + rc.getStudentName()
                    + " was approved and will be sent to the M-Pesa number that paid.");
        }
        return credits.save(rc);
    }

    @Transactional
    public RefundCredit reject(Long id, String reason, String admin) {
        RefundCredit rc = get(id);
        if (rc.getStatus() != RefundStatus.AWAITING_APPROVAL && rc.getStatus() != RefundStatus.AWAITING_PARENT) {
            throw MoneyException.conflict("Credit is " + rc.getStatus() + ".");
        }
        if (reason == null || reason.isBlank()) throw MoneyException.bad("Give a reason.");
        rc.setStatus(RefundStatus.REJECTED);
        rc.setRejectReason(reason.trim());
        rc.setApprovedBy(admin);
        rc.setApprovedAt(LocalDateTime.now());
        credits.save(rc);
        tell(rc, "Your overpayment request was not approved", "The school did not approve the credit for " + rc.getStudentName()
                + ".\nReason: " + rc.getRejectReason());
        return rc;
    }

    /** All parts paid -> the refund is complete. */
    @EventListener
    @Transactional
    public void onSettled(OrderSettledEvent ev) {
        PaymentOrder o = ev.order();
        if (o.getPurpose() != Purpose.REFUND || !"REFUND_PART".equals(o.getSourceType())) return;
        Long creditId = o.getSourceId() / 100;
        credits.findById(creditId).ifPresent(rc -> {
            if (rc.getStatus() != RefundStatus.PAYING) return;
            boolean allPaid = orderRepo.findByPurposeAndSourceType(Purpose.REFUND, "REFUND_PART").stream()
                    .filter(x -> x.getSourceId() / 100 == creditId)
                    .allMatch(x -> x.getStatus() == OrderStatus.PAID || x.getId().equals(o.getId()));
            if (allPaid) { rc.setStatus(RefundStatus.COMPLETED); credits.save(rc); }
        });
    }

    // ---------- reading ----------

    @Transactional(readOnly = true)
    public List<RefundCredit> all() { return credits.findAllByOrderByIdDesc(); }

    @Transactional(readOnly = true)
    public List<RefundCredit> mine(String parentUsername) {
        List<Long> ids = ledger.studentIdsOfParent(parentUsername);
        return ids.isEmpty() ? List.of() : credits.findByStudentIdIn(ids);
    }

    private RefundCredit get(Long id) { return credits.findById(id).orElseThrow(() -> MoneyException.notFound("Credit not found.")); }
}