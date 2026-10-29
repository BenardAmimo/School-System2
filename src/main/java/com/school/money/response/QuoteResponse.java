package com.school.money.response;

import com.school.money.entity.moneyEntities.Quote;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record QuoteResponse(Long id, Long expenseId, Long supplierId, String supplierName, BigDecimal amount,
                            String notes, String recordedBy, LocalDateTime recordedAt) {
    public static QuoteResponse from(Quote e) {
        if (e == null) return null;
        return new QuoteResponse(e.getId(), e.getExpenseId(), e.getSupplierId(), e.getSupplierName(), e.getAmount(),
                e.getNotes(), e.getRecordedBy(), e.getRecordedAt());
    }
    public static List<QuoteResponse> list(List<Quote> l) { return l.stream().map(QuoteResponse::from).toList(); }
}