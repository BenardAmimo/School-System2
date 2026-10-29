package com.school.money.entity.moneyEntities;

import com.school.money.entity.TokenStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** A reward for good work. Admin nominates and sets the amount, SUPER_ADMIN approves. */
@Entity
@Table(name = "money_token", indexes = @Index(columnList = "staff_username,status"))
@Getter @Setter
public class AppreciationToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "staff_username", nullable = false)
    private String staffUsername;
    private String staffName;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 500)
    private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private TokenStatus status = TokenStatus.NOMINATED;
    private String nominatedBy;
    private String decidedBy;
    private String rejectReason;
    private LocalDateTime nominatedAt;
    private LocalDateTime decidedAt;
    @Version
    private Long version;
}