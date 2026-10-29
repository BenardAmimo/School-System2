package com.school.money.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Money carried forward: reduces what a student owes in a later term.
 * FundsService must subtract FeeCreditService.availableFor(studentId, termId) from the fee due.
 */
@Entity
@Table(name = "money_fee_credit",
        uniqueConstraints = @UniqueConstraint(columnNames = {"refund_credit_id"}))
@Getter @Setter
public class FeeCredit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "student_id", nullable = false) private Long studentId;
    /** Term this credit reduces. */
    @Column(nullable = false) private Long targetTermId;
    @Column(name = "refund_credit_id", nullable = false) private Long refundCreditId;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;
    private LocalDateTime createdAt;
}