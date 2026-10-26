package com.school.payouts.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "payouts", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"purpose", "source_type", "source_id"}),
        @UniqueConstraint(columnNames = {"idempotency_key"}),
        @UniqueConstraint(columnNames = {"originator_conversation_id"})})
public class Payout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long payoutId;

    @Version
    private Long version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutPurpose purpose;

    @Column(name = "source_type", nullable = false)
    private String sourceType;                 // e.g. "PAYSLIP", "REFUND", "TEST"

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(nullable = false)
    private String recipientName;

    @Column(nullable = false)
    private String phoneNumber;
    // 2547XXXXXXXX
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PayoutStatus status = PayoutStatus.PREPARED;

    @Enumerated(EnumType.STRING)
    private PayoutChannel channel;

    @Column(nullable = false) private String preparedBy;             // email
    private String approvedBy;                                       // email, never the same as preparedBy

    @Column(name = "idempotency_key", nullable = false, updatable = false)
    private String idempotencyKey = UUID.randomUUID().toString();

    @Column(name = "originator_conversation_id")
    private String originatorConversationId;                         // set when sent
    private String conversationId;
    private String mpesaReceipt;

    @Column(length = 1000)
    private String resultDescription;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime approvedAt;
    private LocalDateTime sentAt;
    private LocalDateTime completedAt;

    public void setResultDescription(String v) {
        this.resultDescription = (v != null && v.length() > 1000) ? v.substring(0, 1000) : v;
    }
}