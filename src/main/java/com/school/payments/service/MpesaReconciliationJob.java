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

    @Scheduled(fixedDelay = 120_000)
    @Transactional
    public void reconcilePendingTransactions() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(3);
        List<MpesaTransactions> stuck = transactionRepository
                .findByStatusAndCreatedAtBefore(Status.PENDING, cutoff);

        for (MpesaTransactions transaction : stuck) {
            try {
                Map<String, Object> result = queryService.queryStatus(transaction.getCheckoutRequestId());
                int resultCode = Integer.parseInt(String.valueOf(result.get("ResultCode")));

                Status newStatus;
                if (resultCode == 0) {
                    newStatus = Status.SUCCESS;
                } else if (resultCode == 1032 || resultCode == 1037 || resultCode == 2001) {
                    newStatus = Status.FAILED;
                } else {
                    newStatus = null;
                }

                if (newStatus != null) {
                    transaction.setStatus(newStatus);
                    transaction.setUpdatedAt(LocalDateTime.now());
                    transactionRepository.save(transaction);

                    idempotencyRepository.findAll().stream()
                            .filter(r -> transaction.getCheckoutRequestId().equals(r.getCheckoutRequestId()))
                            .findFirst()
                            .ifPresent(r -> {
                                r.setStatus(newStatus);
                                idempotencyRepository.save(r);
                            });

                    log.info("Reconciled {} -> {}", transaction.getCheckoutRequestId(), newStatus);
                }
            } catch (Exception ex) {
                log.error("Reconciliation failed for {}", transaction.getCheckoutRequestId(), ex);
            }
        }
    }
}
