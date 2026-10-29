package com.school.money.entity.moneyEntities;

import com.school.money.entity.AdvanceStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "money_advance", indexes = @Index(columnList = "staff_username,status"))
@Getter @Setter
public class SalaryAdvance {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "staff_username", nullable = false)
    private String staffUsername;
    private String staffName;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;
    /** 1 = repay once from the next payroll, up to 4 months. */
    @Column(nullable = false)
    private int instalments;
    @Column(precision = 14, scale = 2)
    private BigDecimal instalmentAmount;
    @Column(precision = 14, scale = 2)
    private BigDecimal repaid = BigDecimal.ZERO;
    /** Salary used for the 3/4 check, kept as evidence. */
    @Column(precision = 14, scale = 2)
    private BigDecimal salaryAtRequest;
    private String reason;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private AdvanceStatus status = AdvanceStatus.REQUESTED;
    /** Repayments made outside payroll, one line each: amount, reference, who recorded it. */
    @Column(length = 1000)
    private String repaymentNotes;
    private String decidedBy;
    private String rejectReason;
    private LocalDateTime requestedAt;
    private LocalDateTime decidedAt;
    @Version
    private Long version;

    @Transient
    public BigDecimal getOutstanding() {
        return amount.subtract(repaid == null ? BigDecimal.ZERO : repaid);
    }
}