package com.school.payments.service;

import com.school.payments.entity.MpesaTransactions;
import com.school.payments.entity.Status;
import com.school.payments.repository.IdempotencyKeyRepo;
import com.school.payments.repository.MpesaTransactionsRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
@Slf4j
@Component
public class MpesaReconciliationJob {

    private final MpesaTransactionsRepository transactionRepository;
    private final IdempotencyKeyRepo idempotencyRepository;
    private final MpesaStkPushQuery queryService;

    public MpesaReconciliationJob(MpesaTransactionsRepository transactionRepository, IdempotencyKeyRepo idempotencyRepository, MpesaStkPushQuery queryService) {
        this.transactionRepository = transactionRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.queryService = queryService;
    }

    @Scheduled(initialDelay = 90_000, fixedDelay = 120_000)
    @Transactional
    public void reconcilePendingTransactions() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(3);
        List<MpesaTransactions> stuck = transactionRepository
                .findByStatusAndCreatedAtBefore(Status.PENDING, cutoff)
                .stream()
                .filter(t -> t.getCheckoutRequestId() != null && !t.getCheckoutRequestId().isBlank())
                .limit(5) // stay safely under the sandbox's 5 requests/60s Spike Arrest limit
                .toList();

        for (MpesaTransactions transaction : stuck) {
            try {
                Map<String, Object> result = queryService.queryStatus(transaction.getCheckoutRequestId());

                if (!result.containsKey("ResultCode")) {
                    log.warn("No ResultCode for {} — treating as FAILED. Raw response: {}",
                            transaction.getCheckoutRequestId(), result);
                    transaction.setStatus(Status.FAILED);
                    transaction.setUpdatedAt(LocalDateTime.now());
                    transactionRepository.save(transaction);
                    continue;
                }

                int resultCode = Integer.parseInt(String.valueOf(result.get("ResultCode")));

                Status newStatus;
                if (resultCode == 0) {
                    newStatus = Status.SUCCESS;
                } else if (resultCode == 1037) {
                    newStatus = Status.FAILED; // timeout, no PIN entered
                } else if (resultCode == 1032) {
                    newStatus = Status.FAILED; // cancelled by user
                } else if (resultCode == 2001) {
                    newStatus = Status.FAILED; // wrong PIN
                } else if (resultCode == 1) {
                    newStatus = Status.FAILED; // insufficient balance
                } else {
                    // Unrecognized code — don't loop forever; if it's old enough
                    // to be in this "stuck" query in the first place, treat as FAILED
                    // rather than leaving it PENDING indefinitely.
                    log.warn("Unhandled ResultCode {} for {} — treating as FAILED",
                            resultCode, transaction.getCheckoutRequestId());
                    newStatus = Status.FAILED;
                }

                transaction.setStatus(newStatus);
                transaction.setUpdatedAt(LocalDateTime.now());
                transactionRepository.save(transaction);

                idempotencyRepository.findByCheckoutRequestId(transaction.getCheckoutRequestId())
                        .ifPresent(r -> {
                            r.setStatus(newStatus);
                            idempotencyRepository.save(r);
                        });

                log.info("Reconciled {} -> {}", transaction.getCheckoutRequestId(), newStatus);

            } catch (Exception ex) {
                log.error("Reconciliation failed for {}", transaction.getCheckoutRequestId(), ex);
            }

            try {
                Thread.sleep(13_000);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}

