package com.school.payments.service;

import com.school.payments.entity.MpesaTransactions;
import com.school.payments.entity.Status;
import com.school.payments.repository.IdempotencyKeyRepo;
import com.school.payments.repository.MpesaTransactionsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Resolves PENDING payments whose callback never arrived.
 *
 * Rules that matter for money:
 *  - Only a definite answer from Safaricom changes a payment's status.
 *  - A missing/unknown answer (error body, rate limit, network failure) leaves it PENDING.
 *  - After a long time with no answer it becomes NEEDS_REVIEW (add that value to the Status enum),
 *    never FAILED, because the customer may have paid.
 *  - No database transaction is held while talking to Safaricom or sleeping.
 */
@Slf4j
@Component
public class MpesaReconciliationJob {

    private static final Duration QUERY_AFTER = Duration.ofMinutes(3);
    private static final Duration GIVE_UP_AFTER = Duration.ofHours(24);
    private static final Duration NO_ID_GIVE_UP_AFTER = Duration.ofMinutes(30);

    // Codes that mean "this request definitely did not result in a payment".
    // Confirm and extend this list against the Daraja docs before relying on it.
    private static final Set<Integer> DEFINITE_FAILURES = Set.of(1, 1032, 1037, 2001);

    private final MpesaTransactionsRepository transactionRepository;
    private final IdempotencyKeyRepo idempotencyRepository;
    private final MpesaStkPushQuery queryService;
    private final TransactionTemplate txTemplate;
    private final int batchSize;
    private final long pauseMs;

    public MpesaReconciliationJob(MpesaTransactionsRepository transactionRepository,
                                  IdempotencyKeyRepo idempotencyRepository,
                                  MpesaStkPushQuery queryService,
                                  TransactionTemplate txTemplate,
                                  @Value("${mpesa.reconcile.batch-size:5}") int batchSize,
                                  @Value("${mpesa.reconcile.pause-ms:13000}") long pauseMs) {
        this.transactionRepository = transactionRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.queryService = queryService;
        this.txTemplate = txTemplate;
        this.batchSize = batchSize;
        this.pauseMs = pauseMs;
    }

    // Deliberately NOT @Transactional: this method makes slow HTTP calls and sleeps.
    @Scheduled(initialDelay = 90_000, fixedDelay = 120_000)
    public void reconcilePendingTransactions() {
        LocalDateTime now = LocalDateTime.now();

        List<MpesaTransactions> pending = transactionRepository
                .findByStatusAndCreatedAtBefore(Status.PENDING, now.minus(QUERY_AFTER))
                .stream()
                .sorted(Comparator.comparing(MpesaTransactions::getCreatedAt)) // oldest first: no starvation
                .toList();

        int queried = 0;
        for (MpesaTransactions t : pending) {
            if (!hasCheckoutId(t)) {
                // The customer may have been prompted but we never stored Safaricom's id.
                // It cannot be queried, so a human has to look at it.
                if (isOlderThan(t, now, NO_ID_GIVE_UP_AFTER)) {
                    resolve(t.getId(), Status.NEEDS_REVIEW, "No checkoutRequestId was stored");
                }
                continue;
            }

            if (queried >= batchSize) break; // stay under Safaricom's request limits
            queried++;

            reconcileOne(t, now);

            if (!pause()) break;
        }
    }

    private void reconcileOne(MpesaTransactions t, LocalDateTime now) {
        String checkoutId = t.getCheckoutRequestId();
        Status decided = null;
        String description = null;

        try {
            Map<String, Object> result = queryService.queryStatus(checkoutId);
            Integer code = parseResultCode(result);

            if (code == null) {
                // An error body (still processing, rate limited, token problem...) is NOT an answer.
                log.warn("No ResultCode for {}; leaving PENDING. Response: {}", checkoutId, result);
            } else if (code == 0) {
                decided = Status.SUCCESS;
                description = "Confirmed by STK query";
            } else if (DEFINITE_FAILURES.contains(code)) {
                decided = Status.FAILED;
                description = result.get("ResultDesc") == null ? "Failed (code " + code + ")"
                        : String.valueOf(result.get("ResultDesc"));
            } else {
                log.warn("Unhandled ResultCode {} for {}; leaving PENDING", code, checkoutId);
            }
        } catch (Exception ex) {
            log.error("Reconciliation query failed for {}; leaving PENDING", checkoutId, ex);
        }

        if (decided != null) {
            resolve(t.getId(), decided, description);
        } else if (isOlderThan(t, now, GIVE_UP_AFTER)) {
            resolve(t.getId(), Status.NEEDS_REVIEW, "No definite answer from Safaricom after 24h");
        }
    }

    /** Short transaction: re-read the row and act only if it is still PENDING (the callback may have won). */
    private void resolve(Long id, Status newStatus, String description) {
        txTemplate.executeWithoutResult(status -> {
            MpesaTransactions t = transactionRepository.findById(id).orElse(null);
            if (t == null || t.getStatus() != Status.PENDING) return;

            t.setStatus(newStatus);
            if (description != null) t.setResultDescription(description);
            t.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(t);

            if (hasCheckoutId(t)) {
                idempotencyRepository.findByCheckoutRequestId(t.getCheckoutRequestId()).ifPresent(r -> {
                    r.setStatus(newStatus);
                    idempotencyRepository.save(r);
                });
            }
            log.info("Reconciled {} -> {}", t.getCheckoutRequestId(), newStatus);
        });
    }

    private boolean hasCheckoutId(MpesaTransactions t) {
        return t.getCheckoutRequestId() != null && !t.getCheckoutRequestId().isBlank();
    }

    private boolean isOlderThan(MpesaTransactions t, LocalDateTime now, Duration age) {
        return t.getCreatedAt() != null && t.getCreatedAt().isBefore(now.minus(age));
    }

    private Integer parseResultCode(Map<String, Object> result) {
        if (result == null || result.get("ResultCode") == null) return null;
        try {
            return Integer.parseInt(String.valueOf(result.get("ResultCode")).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean pause() {
        try {
            Thread.sleep(pauseMs);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}