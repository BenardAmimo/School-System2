package com.school.payouts.response;

import com.school.payouts.entity.Payout;
import com.school.payouts.entity.PayoutChannel;
import com.school.payouts.entity.PayoutPurpose;
import com.school.payouts.entity.PayoutStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PayoutResponse(Long payoutId, Long version, PayoutPurpose purpose, String recipientName,
                             String phoneNumber, BigDecimal amount, PayoutStatus status, PayoutChannel channel,
                             String preparedBy, String approvedBy, String mpesaReceipt, String resultDescription,
                             LocalDateTime createdAt, LocalDateTime sentAt, LocalDateTime completedAt) {

    public static PayoutResponse from(Payout p) {
        return new PayoutResponse(p.getPayoutId(), p.getVersion(), p.getPurpose(), p.getRecipientName(),
                p.getPhoneNumber(), p.getAmount(), p.getStatus(), p.getChannel(), p.getPreparedBy(),
                p.getApprovedBy(), p.getMpesaReceipt(), p.getResultDescription(),
                p.getCreatedAt(), p.getSentAt(), p.getCompletedAt());
    }
}