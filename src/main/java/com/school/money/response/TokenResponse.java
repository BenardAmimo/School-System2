package com.school.money.response;

import com.school.money.entity.TokenStatus;
import com.school.money.entity.moneyEntities.AppreciationToken;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TokenResponse(Long id, String staffUsername, String staffName, BigDecimal amount, String reason,
                            TokenStatus status, String nominatedBy, String decidedBy, String rejectReason, LocalDateTime nominatedAt,
                            LocalDateTime decidedAt) {
    public static TokenResponse from(AppreciationToken e) {
        if (e == null) return null;
        return new TokenResponse(e.getId(), e.getStaffUsername(), e.getStaffName(), e.getAmount(), e.getReason(),
                e.getStatus(), e.getNominatedBy(), e.getDecidedBy(), e.getRejectReason(), e.getNominatedAt(), e.getDecidedAt());
    }
    public static List<TokenResponse> list(List<AppreciationToken> l) { return l.stream().map(TokenResponse::from).toList(); }
}