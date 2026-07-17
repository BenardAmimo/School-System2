package com.school.payments.model;

import java.math.BigDecimal;
import java.util.List;

public record FinanceSummaryResponse(BigDecimal totalCollected,
                                     BigDecimal totalPending,
                                     long transactionCount,
                                     List<RecentTransactionResponse> recentTransactions) {
}
