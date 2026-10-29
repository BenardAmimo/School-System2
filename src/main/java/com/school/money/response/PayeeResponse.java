package com.school.money.response;

import com.school.money.entity.Channel;
import com.school.money.entity.PayeeType;
import com.school.money.entity.moneyEntities.PayeeAccount;

import java.time.LocalDateTime;
import java.util.List;

public record PayeeResponse(Long id, PayeeType payeeType, String payeeRef, String displayName, Channel channel,
                            String phone, String businessNumber, String accountReference, String bankName, String bankAccountNumber,
                            String bankAccountName, boolean verified, String updatedBy, String verifiedBy, LocalDateTime updatedAt,
                            LocalDateTime verifiedAt) {
    public static PayeeResponse from(PayeeAccount e) {
        if (e == null) return null;
        return new PayeeResponse(e.getId(), e.getPayeeType(), e.getPayeeRef(), e.getDisplayName(), e.getChannel(),
                e.getPhone(), e.getBusinessNumber(), e.getAccountReference(), e.getBankName(), e.getBankAccountNumber(),
                e.getBankAccountName(), e.isVerified(), e.getUpdatedBy(), e.getVerifiedBy(), e.getUpdatedAt(), e.getVerifiedAt());
    }
    public static List<PayeeResponse> list(List<PayeeAccount> l) { return l.stream().map(PayeeResponse::from).toList(); }
}