package com.school.money.response;

import com.school.money.entity.ExpenseStatus;
import com.school.money.entity.TenderTier;
import com.school.money.entity.moneyEntities.Expense;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ExpenseResponse(Long id, String title, String description, String category, BigDecimal estimatedAmount,
                              TenderTier tier, LocalDateTime biddingClosesAt, ExpenseStatus status, Long chosenQuoteId, Long supplierId,
                              String supplierName, BigDecimal awardedAmount, String selectionJustification, String requestedBy,
                              String selectedBy, String firstApprover, String finalApprover, String invoiceNumber, BigDecimal invoiceAmount,
                              String receivedBy, LocalDateTime receivedAt, String rejectReason, LocalDateTime createdAt) {
    public static ExpenseResponse from(Expense e) {
        if (e == null) return null;
        return new ExpenseResponse(e.getId(), e.getTitle(), e.getDescription(), e.getCategory(), e.getEstimatedAmount(),
                e.getTier(), e.getBiddingClosesAt(), e.getStatus(), e.getChosenQuoteId(), e.getSupplierId(),
                e.getSupplierName(), e.getAwardedAmount(), e.getSelectionJustification(), e.getRequestedBy(),
                e.getSelectedBy(), e.getFirstApprover(), e.getFinalApprover(), e.getInvoiceNumber(), e.getInvoiceAmount(),
                e.getReceivedBy(), e.getReceivedAt(), e.getRejectReason(), e.getCreatedAt());
    }
    public static List<ExpenseResponse> list(List<Expense> l) { return l.stream().map(ExpenseResponse::from).toList(); }
}