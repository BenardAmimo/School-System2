package com.school.money.entity.moneyEntities;

import com.school.money.entity.PayrollStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "money_payroll_run")
@Getter @Setter
public class PayrollRun {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /** yyyy-MM, one run per month. */
    @Column(nullable = false, unique = true, length = 7)
    private String month;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private PayrollStatus status = PayrollStatus.DRAFT;
    @Column(precision = 14, scale = 2)
    private BigDecimal totalNetPay = BigDecimal.ZERO;
    private String preparedBy;
    private String approvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    @Version
    private Long version;
}