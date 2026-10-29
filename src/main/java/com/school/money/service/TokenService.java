package com.school.money.service;

import com.school.money.Access;
import com.school.money.MoneyException;
import com.school.money.config.MoneyNotifier;
import com.school.money.entity.TokenStatus;
import com.school.money.entity.moneyEntities.AppreciationToken;
import com.school.money.entity.moneyEntities.StaffPayProfile;
import com.school.money.repository.AppreciationTokenRepository;
import com.school.money.request.*;
import com.school.money.response.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Appreciation tokens: an Admin nominates a teacher and sets the amount; ONLY a SUPER_ADMIN approves.
 * Approved tokens are paid with the next payroll and shown on their own line on the payslip.
 */
@Service
public class TokenService {

    private final AppreciationTokenRepository repo;
    private final PayrollService payroll;
    private final MoneyNotifier notifier;

    public TokenService(AppreciationTokenRepository repo, PayrollService payroll, MoneyNotifier notifier) {
        this.repo = repo; this.payroll = payroll; this.notifier = notifier;
    }

    // ---- Entry points for the controller: who may do it, the work, and the mapping to the response ----

    /** Admins nominate and set the amount. */
    @Transactional
    public TokenResponse nominate(TokenRequest r, Authentication auth) {
        return TokenResponse.from(nominate(r.staffUsername(), r.amount(), r.reason(), Access.require(auth, Access.ADMIN)));
    }

    /** Only a Super Admin approves or rejects. */
    @Transactional
    public TokenResponse approve(Long id, Authentication auth) {
        return TokenResponse.from(approve(id, Access.require(auth, Access.SUPER_ADMIN)));
    }

    @Transactional
    public TokenResponse reject(Long id, ReasonRequest r, Authentication auth) {
        return TokenResponse.from(reject(id, r.reason(), Access.require(auth, Access.SUPER_ADMIN)));
    }

    @Transactional(readOnly = true)
    public List<TokenResponse> all(Authentication auth) {
        Access.require(auth, Access.ADMIN, Access.SUPER_ADMIN);
        return TokenResponse.list(all());
    }

    @Transactional(readOnly = true)
    public List<TokenResponse> mine(Authentication auth) {
        return TokenResponse.list(mine(Access.user(auth)));
    }

    // ---- The work itself ----

    @Transactional
    public AppreciationToken nominate(String staffUsername, BigDecimal amount, String reason, String actor) {
        if (staffUsername == null || staffUsername.isBlank()) throw MoneyException.bad("Choose a staff member.");
        if (staffUsername.equalsIgnoreCase(actor)) throw MoneyException.forbidden("You cannot nominate yourself.");
        if (amount == null || amount.signum() <= 0) throw MoneyException.bad("Amount must be above zero.");
        if (amount.stripTrailingZeros().scale() > 0) throw MoneyException.bad("Enter whole shillings.");
        if (reason == null || reason.isBlank()) throw MoneyException.bad("Say why this person deserves the token.");
        StaffPayProfile p = payroll.currentProfile(staffUsername.trim(), LocalDate.now())
                .filter(x -> x.getNetSalary().signum() > 0)
                .orElseThrow(() -> MoneyException.conflict("This person is not on payroll, so a token cannot be paid."));
        AppreciationToken t = new AppreciationToken();
        t.setStaffUsername(p.getStaffUsername());
        t.setStaffName(p.getStaffName());
        t.setAmount(amount);
        t.setReason(reason.trim());
        t.setNominatedBy(actor);
        t.setNominatedAt(LocalDateTime.now());
        repo.save(t);
        if (notifier != null) notifier.toRoles("Appreciation token waiting for approval",
                actor + " nominated " + t.getStaffName() + " for a token of " + amount + ".\nReason: " + t.getReason()
                        + "\nOpen Money out > Tokens to approve or reject.", "SUPER_ADMIN");
        return t;
    }

    /** Callers must already have checked SUPER_ADMIN. The nominator can never approve their own nomination. */
    @Transactional
    public AppreciationToken approve(Long id, String actor) {
        AppreciationToken t = get(id);
        if (t.getStatus() != TokenStatus.NOMINATED) throw MoneyException.conflict("Token is " + t.getStatus() + ".");
        if (actor.equalsIgnoreCase(t.getNominatedBy())) throw MoneyException.forbidden("You nominated this token, so another Super Admin must approve it.");
        t.setStatus(TokenStatus.APPROVED);
        t.setDecidedBy(actor);
        t.setDecidedAt(LocalDateTime.now());
        repo.save(t);
        if (notifier != null) {
            notifier.toUser(t.getStaffUsername(), "An appreciation token for you",
                    "The school wants to thank you: " + t.getReason() + "\nA token of " + t.getAmount() + " will be added to your next pay.");
            notifier.toUser(t.getNominatedBy(), "Your token nomination was approved", t.getStaffName() + "'s token of " + t.getAmount() + " was approved.");
        }
        return t;
    }

    @Transactional
    public AppreciationToken reject(Long id, String reason, String actor) {
        AppreciationToken t = get(id);
        if (t.getStatus() != TokenStatus.NOMINATED) throw MoneyException.conflict("Token is " + t.getStatus() + ".");
        if (reason == null || reason.isBlank()) throw MoneyException.bad("Give a reason.");
        t.setStatus(TokenStatus.REJECTED);
        t.setRejectReason(reason.trim());
        t.setDecidedBy(actor);
        t.setDecidedAt(LocalDateTime.now());
        repo.save(t);
        if (notifier != null) notifier.toUser(t.getNominatedBy(), "Your token nomination was not approved",
                t.getStaffName() + "'s token was not approved.\nReason: " + t.getRejectReason());
        return t;
    }

    @Transactional(readOnly = true)
    public List<AppreciationToken> all() { return repo.findAllByOrderByIdDesc(); }

    @Transactional(readOnly = true)
    public List<AppreciationToken> mine(String u) { return repo.findByStaffUsernameOrderByIdDesc(u); }

    private AppreciationToken get(Long id) { return repo.findById(id).orElseThrow(() -> MoneyException.notFound("Token not found.")); }
}