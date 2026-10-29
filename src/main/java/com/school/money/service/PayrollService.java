package com.school.money.service;

import com.school.money.Access;
import com.school.money.MoneyException;
import com.school.money.config.MoneyNotifier;
import com.school.money.entity.*;
import com.school.money.entity.moneyEntities.*;
import com.school.money.port.StaffDirectoryPort;
import com.school.money.repository.*;
import com.school.money.request.*;
import com.school.money.response.*;
import org.springframework.context.event.EventListener;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

/**
 * Monthly payroll. Salary is NET only. A payslip is
 *   netPay = netSalary - advanceRecovery + approved tokens.
 * The bursar prepares, a different admin approves, approval creates one payment order per payslip.
 */
@Service
public class PayrollService {

    private final StaffPayProfileRepository profiles;
    private final PayrollRunRepository runs;
    private final PayslipRepository slips;
    private final SalaryAdvanceRepository advances;
    private final AppreciationTokenRepository tokens;
    private final PaymentOrderRepository orders;
    private final PayeeAccountRepository payees;
    private final PaymentOrderService orderService;
    private MoneyNotifier notifier;
    private final StaffDirectoryPort directory;

    public PayrollService(StaffPayProfileRepository profiles, PayrollRunRepository runs, PayslipRepository slips,
                          SalaryAdvanceRepository advances, AppreciationTokenRepository tokens,
                          PaymentOrderRepository orders, PayeeAccountRepository payees, PaymentOrderService orderService,
                          MoneyNotifier notifier, StaffDirectoryPort directory) {
        this.profiles = profiles; this.runs = runs; this.slips = slips; this.advances = advances;
        this.tokens = tokens; this.orders = orders; this.payees = payees; this.orderService = orderService;
        this.notifier = notifier; this.directory = directory;
    }

    // ---- Entry points for the controller: who may do it, the work, and the mapping to the response ----

    @Transactional(readOnly = true)
    public List<PayProfileResponse> currentProfiles(Authentication auth) {
        Access.preparer(auth);
        return PayProfileResponse.list(currentProfiles());
    }

    @Transactional(readOnly = true)
    public List<PayProfileResponse> history(String username, Authentication auth) {
        Access.preparer(auth);
        return PayProfileResponse.list(history(username));
    }

    /** Setting pay is an approver's job: the bursar prepares payroll but cannot change what people earn. */
    @Transactional
    public PayProfileResponse setSalary(SalaryRequest r, Authentication auth) {
        return PayProfileResponse.from(setSalary(r.staffUsername(), r.staffName(), r.netSalary(), r.effectiveFrom(), Access.approver(auth)));
    }

    /** A staff member leaves: pay stops after this day and the last month is part-paid. */
    @Transactional
    public PayProfileResponse endEmployment(EndEmploymentRequest r, Authentication auth) {
        return PayProfileResponse.from(endEmployment(r.staffUsername(), r.lastDay(), Access.approver(auth)));
    }

    @Transactional(readOnly = true)
    public List<PayrollRunResponse> listRuns(Authentication auth) {
        Access.preparer(auth);
        return PayrollRunResponse.list(listRuns());
    }

    @Transactional(readOnly = true)
    public List<PayslipResponse> payslips(Long runId, Authentication auth) {
        Access.preparer(auth);
        return PayslipResponse.list(payslips(runId));
    }

    @Transactional
    public PayrollRunResponse prepare(MonthRequest r, Authentication auth) {
        return PayrollRunResponse.from(prepare(r.month(), Access.preparer(auth)));
    }

    @Transactional
    public PayrollRunResponse approve(Long runId, Authentication auth) {
        return PayrollRunResponse.from(approve(runId, Access.approver(auth)));
    }

    @Transactional
    public void delete(Long runId, Authentication auth) {
        Access.preparer(auth);
        delete(runId);
    }

    /** The signed-in staff member's own payslips (drafts are not shown). */
    @Transactional(readOnly = true)
    public List<MyPayslipResponse> myPayslips(Authentication auth) {
        List<MyPayslipResponse> out = new ArrayList<>();
        for (Payslip s : slips.findByStaffUsernameOrderByIdDesc(Access.user(auth))) {
            PayrollRun r = runs.findById(s.getRunId()).orElse(null);
            if (r == null || r.getStatus() == PayrollStatus.DRAFT) continue;
            out.add(new MyPayslipResponse(r.getMonth(), s.getNetSalary(), s.getFullSalary(), s.getStaffName(),
                    s.getAdvanceRecovery(), s.getTokenAmount(), s.getNetPay(), s.isSettled()));
        }
        return out;
    }

    // ---------- pay profiles ----------

    @Transactional
    public StaffPayProfile setSalary(String staffUsername, String staffName, BigDecimal netSalary, LocalDate from, String actor) {
        if (staffUsername == null || staffUsername.isBlank()) throw MoneyException.bad("Staff username is required.");
        if (netSalary == null || netSalary.signum() <= 0) throw MoneyException.bad("Net salary must be above zero.");
        if (from == null) throw MoneyException.bad("Effective date is required.");
        StaffPayProfile p = new StaffPayProfile();
        p.setStaffUsername(staffUsername.trim());
        p.setStaffName(staffName == null || staffName.isBlank()
                ? (directory == null ? staffUsername.trim() : directory.nameOf(staffUsername.trim())) : staffName.trim());
        p.setNetSalary(netSalary);
        p.setEffectiveFrom(from);
        p.setCreatedBy(actor);
        return profiles.save(p);
    }

    @Transactional(readOnly = true)
    public Optional<StaffPayProfile> currentProfile(String username, LocalDate on) {
        return profiles.findFirstByStaffUsernameAndEffectiveFromLessThanEqualOrderByEffectiveFromDescIdDesc(username, on);
    }

    @Transactional(readOnly = true)
    public List<StaffPayProfile> currentProfiles() {
        List<StaffPayProfile> out = new ArrayList<>();
        for (String u : profiles.findAllStaffUsernames()) {
            currentProfile(u, LocalDate.now()).filter(p -> p.getNetSalary().signum() > 0).ifPresent(out::add);   // ended staff are hidden
        }
        out.sort(Comparator.comparing(StaffPayProfile::getStaffName, String.CASE_INSENSITIVE_ORDER));
        return out;
    }

    @Transactional(readOnly = true)
    public List<StaffPayProfile> history(String username) {
        return profiles.findByStaffUsernameOrderByEffectiveFromDesc(username);
    }

    // ---------- runs ----------

    /** Builds (or rebuilds, while still DRAFT) the payslips for a month. */
    @Transactional
    public PayrollRun prepare(String month, String actor) {
        YearMonth ym;
        try { ym = YearMonth.parse(month); } catch (Exception e) { throw MoneyException.bad("Month must look like 2026-10."); }
        PayrollRun run = runs.findByMonth(month).orElse(null);
        if (run != null && run.getStatus() != PayrollStatus.DRAFT) {
            throw MoneyException.conflict("Payroll for " + month + " is already " + run.getStatus() + ".");
        }
        if (run == null) {
            run = new PayrollRun();
            run.setMonth(month);
            run.setCreatedAt(LocalDateTime.now());
            run = runs.save(run);
        } else {
            release(run);
        }
        run.setPreparedBy(actor);

        LocalDate end = ym.atEndOfMonth();
        BigDecimal total = BigDecimal.ZERO;
        for (String u : profiles.findAllStaffUsernames()) {
            List<StaffPayProfile> hist = profiles.findByStaffUsernameOrderByEffectiveFromDesc(u);
            BigDecimal salary = prorate(hist, ym);
            if (salary.signum() <= 0) continue;            // not employed yet, or already left, for the whole month
            StaffPayProfile p = hist.get(0);
            Payslip s = new Payslip();
            s.setRunId(run.getId());
            s.setStaffUsername(u);
            s.setStaffName(p.getStaffName());
            s.setNetSalary(salary);
            s.setFullSalary(currentProfile(u, end).map(StaffPayProfile::getNetSalary).orElse(salary));

            advances.findFirstByStaffUsernameAndStatus(u, AdvanceStatus.ACTIVE).ifPresent(a -> {
                BigDecimal rec = recovery(a, salary);
                if (rec.signum() > 0) { s.setAdvanceRecovery(rec); s.setAdvanceId(a.getId()); }
            });

            BigDecimal tok = BigDecimal.ZERO;
            List<String> ids = new ArrayList<>();
            for (AppreciationToken t : tokens.findByStaffUsernameAndStatus(u, TokenStatus.APPROVED)) {
                tok = tok.add(t.getAmount());
                ids.add(String.valueOf(t.getId()));
                t.setStatus(TokenStatus.IN_PAYROLL);
                tokens.save(t);
            }
            s.setTokenAmount(tok);
            s.setTokenIds(String.join(",", ids));
            s.setNetPay(s.getNetSalary().subtract(s.getAdvanceRecovery()).add(tok));
            slips.save(s);
            total = total.add(s.getNetPay());
        }
        run.setTotalNetPay(total);
        return runs.save(run);
    }

    /**
     * Salary for the days actually employed in a month. Each day counts the monthly salary in force on that day
     * divided by the days of the month, so a start, a raise or a leaving date in mid-month is handled the same way.
     * A whole month at one salary returns that salary unchanged; otherwise the result is rounded to whole shillings.
     */
    public static BigDecimal prorate(List<StaffPayProfile> history, YearMonth ym) {
        int days = ym.lengthOfMonth();
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal first = null;
        boolean uniform = true;
        for (int d = 1; d <= days; d++) {
            LocalDate date = ym.atDay(d);
            StaffPayProfile inForce = null;
            for (StaffPayProfile p : history) {
                if (!p.getEffectiveFrom().isAfter(date) && (inForce == null || p.getEffectiveFrom().isAfter(inForce.getEffectiveFrom())
                        || (p.getEffectiveFrom().equals(inForce.getEffectiveFrom()) && p.getId() != null && inForce.getId() != null && p.getId() > inForce.getId()))) {
                    inForce = p;
                }
            }
            BigDecimal day = inForce == null ? BigDecimal.ZERO : inForce.getNetSalary();
            if (first == null) first = day;
            else if (day.compareTo(first) != 0) uniform = false;
            sum = sum.add(day);
        }
        if (uniform) return first == null ? BigDecimal.ZERO : first;
        return sum.divide(BigDecimal.valueOf(days), 0, RoundingMode.HALF_UP);
    }

    /** Stops pay after the last working day. Earlier months are untouched; the final month is part-paid. */
    @Transactional
    public StaffPayProfile endEmployment(String staffUsername, LocalDate lastDay, String actor) {
        if (lastDay == null) throw MoneyException.bad("Enter the last working day.");
        List<StaffPayProfile> hist = profiles.findByStaffUsernameOrderByEffectiveFromDesc(staffUsername);
        if (hist.isEmpty()) throw MoneyException.notFound("No salary on record for " + staffUsername + ".");
        StaffPayProfile p = new StaffPayProfile();
        p.setStaffUsername(staffUsername);
        p.setStaffName(hist.get(0).getStaffName());
        p.setNetSalary(BigDecimal.ZERO);
        p.setEffectiveFrom(lastDay.plusDays(1));
        p.setCreatedBy(actor);
        return profiles.save(p);
    }

    /** True while a payroll that is not yet fully paid holds an instalment of this advance. */
    @Transactional(readOnly = true)
    public boolean hasOpenRecovery(Long advanceId) {
        return slips.existsByAdvanceIdAndSettledFalse(advanceId);
    }

    /** What the next payroll takes back for this advance: one instalment, never more than is owed or earned. */
    public static BigDecimal recovery(SalaryAdvance a, BigDecimal netSalary) {
        BigDecimal r = a.getInstalmentAmount().min(a.getOutstanding()).min(netSalary);
        return r.signum() < 0 ? BigDecimal.ZERO : r;
    }

    private void release(PayrollRun run) {
        for (Payslip s : slips.findByRunIdOrderByStaffName(run.getId())) {
            if (s.getTokenIds() != null && !s.getTokenIds().isBlank()) {
                for (String id : s.getTokenIds().split(",")) {
                    tokens.findById(Long.parseLong(id)).ifPresent(t -> {
                        if (t.getStatus() == TokenStatus.IN_PAYROLL) { t.setStatus(TokenStatus.APPROVED); tokens.save(t); }
                    });
                }
            }
            slips.delete(s);
        }
        slips.flush();
    }

    /** Approve: a DIFFERENT admin from the preparer. Creates the payment orders. */
    @Transactional
    public PayrollRun approve(Long runId, String actor) {
        PayrollRun run = run(runId);
        if (run.getStatus() != PayrollStatus.DRAFT) throw MoneyException.conflict("Payroll is already " + run.getStatus() + ".");
        if (actor.equalsIgnoreCase(run.getPreparedBy())) throw MoneyException.forbidden("You prepared this payroll, so someone else must approve it.");
        List<Payslip> list = slips.findByRunIdOrderByStaffName(runId);
        if (list.isEmpty()) throw MoneyException.bad("This payroll has no payslips.");

        List<String> missing = new ArrayList<>();
        for (Payslip s : list) {
            if (s.getNetPay().signum() <= 0) continue;
            var a = payees.findByPayeeTypeAndPayeeRef(PayeeType.STAFF, s.getStaffUsername());
            if (a.isEmpty() || !a.get().isVerified()) missing.add(s.getStaffName());
        }
        if (!missing.isEmpty()) {
            throw MoneyException.conflict("These staff have no verified payment details: " + String.join(", ", missing));
        }

        run.setStatus(PayrollStatus.APPROVED);
        run.setApprovedBy(actor);
        run.setApprovedAt(LocalDateTime.now());
        runs.save(run);
        for (Payslip s : list) {
            if (s.getNetPay().signum() <= 0) {
                settleSlip(s);   // nothing to send: salary fully taken by the advance
                slips.save(s);
                continue;
            }
            PaymentOrder o = orderService.create(Purpose.SALARY, "PAYSLIP", s.getId(), s.getNetPay(),
                    PayeeType.STAFF, s.getStaffUsername(), "Salary " + run.getMonth(), actor);
            s.setPaymentOrderId(o.getId());
            slips.save(s);
        }
        completeIfDone(run);
        if (notifier != null) {
            for (Payslip s : list) {
                notifier.toUser(s.getStaffUsername(), "Your " + run.getMonth() + " pay is on its way",
                        "Hello " + s.getStaffName() + ",\n\nYour pay for " + run.getMonth() + " has been approved.\n"
                                + "  Net salary:        " + s.getNetSalary() + (s.getFullSalary().compareTo(s.getNetSalary()) != 0 ? "  (part month, full month is " + s.getFullSalary() + ")" : "") + "\n"
                                + (s.getAdvanceRecovery().signum() > 0 ? "  Advance recovery: -" + s.getAdvanceRecovery() + "\n" : "")
                                + (s.getTokenAmount().signum() > 0 ? "  Appreciation token: +" + s.getTokenAmount() + "\n" : "")
                                + "  Net pay:           " + s.getNetPay() + "\n\nYou can see and print your payslip under Money out > My pay.");
            }
        }
        return run;
    }

    @Transactional
    public void delete(Long runId) {
        PayrollRun run = run(runId);
        if (run.getStatus() != PayrollStatus.DRAFT) throw MoneyException.conflict("Only a DRAFT payroll can be deleted.");
        release(run);
        runs.delete(run);
    }

    /** When a salary order is paid: take the instalment, close the advance when done, mark tokens paid. */
    @EventListener
    @Transactional
    public void onSettled(OrderSettledEvent ev) {
        PaymentOrder o = ev.order();
        if (o.getPurpose() != Purpose.SALARY || !"PAYSLIP".equals(o.getSourceType())) return;
        Payslip s = slips.findById(o.getSourceId()).orElse(null);
        if (s == null || s.isSettled()) return;
        settleSlip(s);
        slips.save(s);
        runs.findById(s.getRunId()).ifPresent(this::completeIfDone);
    }

    private void settleSlip(Payslip s) {
        if (s.isSettled()) return;
        if (s.getAdvanceId() != null && s.getAdvanceRecovery().signum() > 0) {
            advances.findById(s.getAdvanceId()).ifPresent(a -> {
                // never take more than is still owed, whatever happened since the payslip was prepared
                BigDecimal take = s.getAdvanceRecovery().min(a.getOutstanding().max(BigDecimal.ZERO));
                a.setRepaid(a.getRepaid().add(take));
                if (a.getOutstanding().signum() <= 0) a.setStatus(AdvanceStatus.CLOSED);
                advances.save(a);
            });
        }
        if (s.getTokenIds() != null && !s.getTokenIds().isBlank()) {
            for (String id : s.getTokenIds().split(",")) {
                tokens.findById(Long.parseLong(id)).ifPresent(t -> { t.setStatus(TokenStatus.PAID); tokens.save(t); });
            }
        }
        s.setSettled(true);
    }

    private void completeIfDone(PayrollRun run) {
        if (run.getStatus() != PayrollStatus.APPROVED) return;
        boolean all = slips.findByRunIdOrderByStaffName(run.getId()).stream().allMatch(Payslip::isSettled);
        if (all) { run.setStatus(PayrollStatus.PAID); runs.save(run); }
    }

    // ---------- reading ----------

    @Transactional(readOnly = true)
    public List<PayrollRun> listRuns() { return runs.findAllByOrderByMonthDesc(); }

    @Transactional(readOnly = true)
    public List<Payslip> payslips(Long runId) { return slips.findByRunIdOrderByStaffName(runId); }

    private PayrollRun run(Long id) { return runs.findById(id).orElseThrow(() -> MoneyException.notFound("Payroll run not found.")); }
}