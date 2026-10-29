package com.school.money.entity.moneyEntities;

import com.school.money.entity.Channel;
import com.school.money.entity.OrderStatus;
import com.school.money.entity.Purpose;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * One instruction to pay one payee. Created only AFTER the business approval.
 * The unique key makes creation idempotent: the same source can never be paid twice.
 */
@Entity
@Table(name = "money_payment_order",
        uniqueConstraints = @UniqueConstraint(columnNames = {"purpose", "source_type", "source_id"}),
        indexes = {@Index(columnList = "status"), @Index(columnList = "batch_id")})
@Getter @Setter
public class PaymentOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Purpose purpose;
    @Column(name = "source_type", nullable = false, length = 40)
    private String sourceType;
    @Column(name = "source_id", nullable = false)
    private Long sourceId;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;

    // Snapshot of the payee details at the time of creation (later edits must not redirect this payment).
    private String payeeName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Channel channel;
    private String phone;
    private String businessNumber;
    private String accountReference;
    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.APPROVED;
    private String description;
    /** M-Pesa receipt, bank reference, cheque number or cash voucher number. */
    private String reference;
    private String failureReason;
    /** Links the B2B request to its callback. */
    @Column(unique = true)
    private String originatorConversationId;
    /** Id of the matching row in the existing payouts table (B2C only). */
    private Long payoutId;
    @Column(name = "batch_id")
    private String batchId;
    /** Safaricom's charge for this payment (from app.money.fees.tiers), for the expense report. */
    @Column(precision = 14, scale = 2)
    private BigDecimal feeCharged;
    /** Last time Safaricom was asked what happened to a NEEDS_REVIEW order. */
    private LocalDateTime lastStatusCheckAt;

    private String createdBy;
    private String approvedBy;
    private String paidBy;
    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime paidAt;
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}