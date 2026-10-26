
package com.school.payments.service;

import com.school.entity.Student;
import com.school.payments.entity.MpesaTransactions;
import com.school.payments.entity.Status;
import com.school.payments.model.FinanceSummaryResponse;
import com.school.payments.model.RecentTransactionResponse;
import com.school.payments.repository.MpesaTransactionsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FinanceService {
    private final MpesaTransactionsRepository transactionRepository;

    public FinanceService(MpesaTransactionsRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public FinanceSummaryResponse getSummary() {
        BigDecimal totalCollected = orZero(transactionRepository.sumAmountByStatus(Status.SUCCESS));
        BigDecimal totalPending = orZero(transactionRepository.sumAmountByStatus(Status.PENDING));
        long count = transactionRepository.countByStatus(Status.SUCCESS);

        List<RecentTransactionResponse> recent = transactionRepository
                .findRecentWithStudent(PageRequest.of(0, 10))
                .stream()
                .map(t -> new RecentTransactionResponse(
                        payerLabel(t),
                        t.getAmount(),
                        t.getStatus() == null ? "UNKNOWN" : t.getStatus().name(),
                        t.getCreatedAt()))
                .toList();

        return new FinanceSummaryResponse(totalCollected, totalPending, count, recent);
    }

    /** Older payments have no student recorded, so never assume one exists. */
    private String payerLabel(MpesaTransactions t) {
        Student s = t.getStudent();
        if (s != null) {
            return (nullToEmpty(s.getFirstName()) + " " + nullToEmpty(s.getLastName())).trim();
        }
        String ref = t.getAccountReference();
        return (ref == null || ref.isBlank()) ? "Unknown student" : ref;
    }

    private static String nullToEmpty(String v) {
        return v == null ? "" : v;
    }

    private static BigDecimal orZero(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}