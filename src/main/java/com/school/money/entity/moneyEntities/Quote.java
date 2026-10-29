package com.school.money.entity.moneyEntities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "money_quote",
        uniqueConstraints = @UniqueConstraint(columnNames = {"expense_id", "supplier_id"}))
@Getter @Setter
public class Quote {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "expense_id", nullable = false)
    private Long expenseId;
    @Column(name = "supplier_id", nullable = false)
    private Long supplierId;
    private String supplierName;
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal amount;
    @Column(length = 500)
    private String notes;
    private String recordedBy;
    private LocalDateTime recordedAt;
}