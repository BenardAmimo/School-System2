package com.school.money.response;

import com.school.money.entity.RefundChoice;
import com.school.money.entity.RefundCredit;
import com.school.money.entity.RefundStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record RefundResponse(Long id, Long studentId, String studentName, Long termId, BigDecimal amount,
                             RefundStatus status, RefundChoice choice, boolean autoDecided, LocalDateTime decisionDeadline,
                             LocalDateTime decidedAt, String decidedBy, String approvedBy, LocalDateTime approvedAt, String rejectReason,
                             LocalDateTime createdAt) {
    public static RefundResponse from(RefundCredit e) {
        if (e == null) return null;
        return new RefundResponse(e.getId(), e.getStudentId(), e.getStudentName(), e.getTermId(), e.getAmount(),
                e.getStatus(), e.getChoice(), e.isAutoDecided(), e.getDecisionDeadline(), e.getDecidedAt(), e.getDecidedBy(),
                e.getApprovedBy(), e.getApprovedAt(), e.getRejectReason(), e.getCreatedAt());
    }
    public static List<RefundResponse> list(List<RefundCredit> l) { return l.stream().map(RefundResponse::from).toList(); }
}