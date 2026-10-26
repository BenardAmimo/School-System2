package com.school.payouts.service;

import com.school.payouts.entity.Payout;
import com.school.payouts.entity.PayoutChannel;
import com.school.payouts.entity.PayoutStatus;
import com.school.payouts.repository.PayoutRepository;
import com.school.payouts.requests.ManualPaidRequest;
import com.school.payouts.requests.PayoutDecisionRequest;
import com.school.payouts.requests.PreparePayoutRequest;
import com.school.payouts.requests.SettleRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PayoutService {

    private final PayoutRepository payouts;

    @Value("${app.payout.daily-limit:500000}")
    private BigDecimal dailyLimit;

    @Transactional
    public Payout prepare(PreparePayoutRequest r, Authentication auth) {
        String sourceType = r.sourceType().trim().toUpperCase();
        if (payouts.existsByPurposeAndSourceTypeAndSourceId(r.purpose(), sourceType, r.sourceId()))
            throw new IllegalStateException("A payout already exists for this " + sourceType + " (#" + r.sourceId() + ").");

        Payout p = new Payout();
        p.setPurpose(r.purpose());
        p.setSourceType(sourceType);
        p.setSourceId(r.sourceId());
        p.setRecipientName(r.recipientName().trim());
        p.setPhoneNumber(normalizePhone(r.phoneNumber()));
        p.setAmount(r.amount());
        p.setReason(r.reason());
        p.setPreparedBy(auth.getName());
        return payouts.save(p);
    }

    /** The checker approves. It cannot be the person who prepared it. */
    @Transactional
    public Payout approve(Long id, Long version, Authentication auth) {
        Payout p = find(id, version);
        require(p, PayoutStatus.PREPARED, "approved");
        if (p.getPreparedBy().equalsIgnoreCase(auth.getName()))
            throw new AccessDeniedException("The person who prepared a payout cannot approve it.");

        BigDecimal today = payouts.sumApprovedSince(
                List.of(PayoutStatus.APPROVED, PayoutStatus.SENT, PayoutStatus.SUCCESS, PayoutStatus.NEEDS_REVIEW),
                LocalDate.now(ZoneId.of("Africa/Nairobi")).atStartOfDay());
        if (today.add(p.getAmount()).compareTo(dailyLimit) > 0)
            throw new IllegalStateException("This would exceed today's payout limit of " + dailyLimit + ".");

        p.setStatus(PayoutStatus.APPROVED);
        p.setApprovedBy(auth.getName());
        p.setApprovedAt(LocalDateTime.now());
        return p;
    }

    @Transactional
    public Payout reject(Long id, PayoutDecisionRequest r, Authentication auth) {
        Payout p = find(id, r.version());
        require(p, PayoutStatus.PREPARED, "rejected");
        if (r.comment() == null || r.comment().isBlank())
            throw new IllegalStateException("Give a reason when rejecting a payout.");
        p.setStatus(PayoutStatus.REJECTED);
        p.setResultDescription(r.comment().trim());
        p.setApprovedBy(auth.getName());
        return p;
    }

    /** Fallback while B2C is not live: someone paid outside the system and records the M-Pesa reference. */
    @Transactional
    public Payout markPaidManually(Long id, ManualPaidRequest r, Authentication auth) {
        Payout p = find(id, r.version());
        require(p, PayoutStatus.APPROVED, "marked as paid");
        p.setChannel(PayoutChannel.MANUAL);
        p.setMpesaReceipt(r.reference().trim());
        p.setStatus(PayoutStatus.SUCCESS);
        p.setResultDescription("Paid manually; recorded by " + auth.getName());
        p.setCompletedAt(LocalDateTime.now());
        return p;
    }

    /** A FAILED payout starts again as PREPARED and needs a fresh approval. */
    @Transactional
    public Payout retry(Long id, Long version, Authentication auth) {
        Payout p = find(id, version);
        require(p, PayoutStatus.FAILED, "retried");
        p.setStatus(PayoutStatus.PREPARED);
        p.setPreparedBy(auth.getName());
        p.setApprovedBy(null);
        p.setApprovedAt(null);
        p.setOriginatorConversationId(null);
        p.setConversationId(null);
        p.setSentAt(null);
        p.setCompletedAt(null);
        p.setResultDescription(null);
        return p;
    }

    /** A person settles a payout the system could not. They must check the M-Pesa statement first. */
    @Transactional
    public Payout settleReview(Long id, SettleRequest r, Authentication auth) {
        Payout p = find(id, r.version());
        require(p, PayoutStatus.NEEDS_REVIEW, "settled");
        if (r.paid()) {
            if (r.reference() == null || r.reference().isBlank())
                throw new IllegalStateException("Enter the M-Pesa receipt from the statement.");
            p.setStatus(PayoutStatus.SUCCESS);
            p.setMpesaReceipt(r.reference().trim());
        } else {
            if (r.comment() == null || r.comment().isBlank())
                throw new IllegalStateException("Say why you are marking this as not paid.");
            p.setStatus(PayoutStatus.FAILED);
        }
        p.setResultDescription("Settled by " + auth.getName()
                + (r.comment() == null || r.comment().isBlank() ? "" : ": " + r.comment().trim()));
        p.setCompletedAt(LocalDateTime.now());
        return p;
    }

    @Transactional(readOnly = true)
    public List<Payout> list(PayoutStatus status) {
        return status == null ? payouts.findAllByOrderByCreatedAtDesc()
                : payouts.findByStatusOrderByCreatedAtDesc(status);
    }

    // ---------- helpers ----------

    private Payout find(Long id, Long version) {
        Payout p = payouts.findById(id).orElseThrow(() -> new IllegalArgumentException("Payout not found"));
        if (version == null || !version.equals(p.getVersion()))
            throw new ObjectOptimisticLockingFailureException(Payout.class, id);
        return p;
    }

    private void require(Payout p, PayoutStatus expected, String action) {
        if (p.getStatus() != expected)
            throw new IllegalStateException("A payout that is " + p.getStatus() + " cannot be " + action + ".");
    }

    /** Accepts 07XXXXXXXX, 01XXXXXXXX, +2547..., 2547...; stores 2547XXXXXXXX. */
    static String normalizePhone(String raw) {
        String d = raw.replaceAll("[\\s+\\-]", "");
        if (d.startsWith("0")) d = "254" + d.substring(1);
        if (!d.matches("254[17]\\d{8}"))
            throw new IllegalArgumentException("Enter a valid Safaricom number, like 0712345678.");
        return d;
    }
}