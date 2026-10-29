package com.school.money.response;

import com.school.money.entity.PayrollStatus;
import com.school.money.entity.moneyEntities.PayrollRun;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PayrollRunResponse(Long id, String month, PayrollStatus status, BigDecimal totalNetPay, String preparedBy,
                                 String approvedBy, LocalDateTime createdAt, LocalDateTime approvedAt) {
    public static PayrollRunResponse from(PayrollRun e) {
        if (e == null) return null;
        return new PayrollRunResponse(e.getId(), e.getMonth(), e.getStatus(), e.getTotalNetPay(), e.getPreparedBy(),
                e.getApprovedBy(), e.getCreatedAt(), e.getApprovedAt());
    }
    public static List<PayrollRunResponse> list(List<PayrollRun> l) { return l.stream().map(PayrollRunResponse::from).toList(); }
}