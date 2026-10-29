package com.school.money.entity.moneyEntities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Net monthly salary for a staff member, effective from a date. History is kept, never edited. */
@Entity
@Table(name = "money_pay_profile", indexes = @Index(columnList = "staff_username,effective_from"))
@Getter @Setter
public class StaffPayProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "staff_username", nullable = false)
    private String staffUsername;
    private String staffName;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal netSalary;
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
    private String createdBy;
}