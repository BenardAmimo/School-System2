package com.school.money.response;

import com.school.money.entity.AdvanceStatus;
import com.school.money.entity.moneyEntities.SalaryAdvance;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdvanceResponse(Long id, String staffUsername, String staffName, BigDecimal amount, int instalments,
                              BigDecimal instalmentAmount, BigDecimal repaid, BigDecimal salaryAtRequest, String reason, AdvanceStatus status,
                              String repaymentNotes, String decidedBy, String rejectReason, LocalDateTime requestedAt, LocalDateTime decidedAt) {
    public static AdvanceResponse from(SalaryAdvance e) {
        if (e == null) return null;
        return new AdvanceResponse(e.getId(), e.getStaffUsername(), e.getStaffName(), e.getAmount(), e.getInstalments(),
                e.getInstalmentAmount(), e.getRepaid(), e.getSalaryAtRequest(), e.getReason(), e.getStatus(),
                e.getRepaymentNotes(), e.getDecidedBy(), e.getRejectReason(), e.getRequestedAt(), e.getDecidedAt());
    }
    public static List<AdvanceResponse> list(List<SalaryAdvance> l) { return l.stream().map(AdvanceResponse::from).toList(); }
}