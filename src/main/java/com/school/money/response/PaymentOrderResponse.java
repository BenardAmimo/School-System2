package com.school.money.response;

import com.school.money.entity.Channel;
import com.school.money.entity.OrderStatus;
import com.school.money.entity.Purpose;
import com.school.money.entity.moneyEntities.PaymentOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PaymentOrderResponse(Long id, Purpose purpose, String sourceType, Long sourceId, BigDecimal amount,
                                   String payeeName, Channel channel, String phone, String businessNumber, String accountReference, String bankName,
                                   String bankAccountNumber, String bankAccountName, OrderStatus status, String description, String reference,
                                   String failureReason, String batchId, BigDecimal feeCharged, LocalDateTime lastStatusCheckAt, String createdBy,
                                   String approvedBy, String paidBy, LocalDateTime createdAt, LocalDateTime sentAt, LocalDateTime paidAt,
                                   LocalDateTime updatedAt) {
    public static PaymentOrderResponse from(PaymentOrder e) {
        if (e == null) return null;
        return new PaymentOrderResponse(e.getId(), e.getPurpose(), e.getSourceType(), e.getSourceId(), e.getAmount(),
                e.getPayeeName(), e.getChannel(), e.getPhone(), e.getBusinessNumber(), e.getAccountReference(), e.getBankName(),
                mask(e.getBankAccountNumber()), e.getBankAccountName(), e.getStatus(), e.getDescription(), e.getReference(),
                e.getFailureReason(), e.getBatchId(), e.getFeeCharged(), e.getLastStatusCheckAt(), e.getCreatedBy(),
                e.getApprovedBy(), e.getPaidBy(), e.getCreatedAt(), e.getSentAt(), e.getPaidAt(), e.getUpdatedAt());
    }
    public static List<PaymentOrderResponse> list(List<PaymentOrder> l) { return l.stream().map(PaymentOrderResponse::from).toList(); }
    private static String mask(String v) {
        if (v == null || v.length() <= 4) return v;
        return "****" + v.substring(v.length() - 4);
    }
}