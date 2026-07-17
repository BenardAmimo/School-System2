package com.school.payments.service;

import com.school.payments.entity.Status;
import com.school.payments.repository.MpesaTransactionsRepository;
import com.school.payments.model.FinanceSummaryResponse;
import com.school.payments.model.RecentTransactionResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FinanceService {
    private final MpesaTransactionsRepository transactionRepository;

    public FinanceService(MpesaTransactionsRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public FinanceSummaryResponse getSummary() {
        BigDecimal totalCollected = transactionRepository.sumAmountByStatus(Status.SUCCESS);
        BigDecimal totalPending = transactionRepository.sumAmountByStatus(Status.PENDING);
        long count = transactionRepository.countByStatus(Status.SUCCESS);

        List<RecentTransactionResponse> recent = transactionRepository
                .findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(t -> new RecentTransactionResponse(
                        t.getFunds().getStudents().getUserReg().getFirstName() + " " + t.getFunds().getStudents().getUserReg().getLastName(),
                        t.getAmount(),
                        t.getStatus().name(),
                        t.getCreatedAt()))
                .toList();

        return new FinanceSummaryResponse(totalCollected, totalPending, count, recent);
    }
}
