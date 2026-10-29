package com.school.money.entity.moneyEntities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** netPay = netSalary - advanceRecovery + tokenAmount. Salary is net only (no statutory lines yet). */
@Entity
@Table(name = "money_payslip",
        uniqueConstraints = @UniqueConstraint(columnNames = {"run_id", "staff_username"}))
@Getter @Setter
public class Payslip {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "run_id", nullable = false)
    private Long runId;
    @Column(name = "staff_username", nullable = false)
    private String staffUsername;
    private String staffName;
    /** Salary for the days actually worked in the month (equals fullSalary for a whole month). */
    @Column(precision = 14, scale = 2) private BigDecimal netSalary = BigDecimal.ZERO;
    /** The full monthly salary at the end of the month, shown next to a part-month amount. */
    @Column(precision = 14, scale = 2) private BigDecimal fullSalary = BigDecimal.ZERO;
    @Column(precision = 14, scale = 2) private BigDecimal advanceRecovery = BigDecimal.ZERO;
    @Column(precision = 14, scale = 2) private BigDecimal tokenAmount = BigDecimal.ZERO;
    @Column(precision = 14, scale = 2) private BigDecimal netPay = BigDecimal.ZERO;
    /** Advance this recovery belongs to (null when none). */
    private Long advanceId;
    /** Tokens included on this payslip, comma separated (null when none). */
    @Column(length = 500)
    private String tokenIds;
    private Long paymentOrderId;
    /** True once the money reached the staff member and the advance/tokens were settled. */
    private boolean settled;
}