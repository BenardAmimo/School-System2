package com.school.money.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** An overpayment for one student in one term. At most one per student and term: no double use. */
@Entity
@Table(name = "money_refund_credit",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "term_id"}))
@Getter @Setter
public class RefundCredit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "student_id", nullable = false) private Long studentId;
    private String studentName;
    @Column(name = "term_id", nullable = false) private Long termId;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private RefundStatus status = RefundStatus.AWAITING_PARENT;
    @Enumerated(EnumType.STRING) @Column(length = 20)
    private RefundChoice choice;
    /** True when the parent never answered and carry-forward was applied by default. */
    private boolean autoDecided;

    private LocalDateTime decisionDeadline;
    private LocalDateTime decidedAt;
    private String decidedBy;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private String rejectReason;
    private LocalDateTime createdAt;
    @Version
    private Long version;
}