package com.school.money.entity.moneyEntities;

import com.school.money.entity.ExpenseStatus;
import com.school.money.entity.TenderTier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "money_expense")
@Getter @Setter
public class Expense {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(length = 1000)
    private String description;
    private String category;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal estimatedAmount;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private TenderTier tier;
    /** Tender only: quotes cannot be awarded before this moment. */
    private LocalDateTime biddingClosesAt;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 24)
    private ExpenseStatus status = ExpenseStatus.REQUESTED;

    private Long chosenQuoteId;
    private Long supplierId;
    private String supplierName;
    @Column(precision = 14, scale = 2)
    private BigDecimal awardedAmount;
    /** Required when the chosen quote is not the cheapest. */
    @Column(length = 500)
    private String selectionJustification;

    private String requestedBy;
    /** Person who chose the supplier. Cannot also approve. */
    private String selectedBy;
    private String firstApprover;
    private String finalApprover;

    // Receipt of goods / service
    private String invoiceNumber;
    @Column(precision = 14, scale = 2)
    private BigDecimal invoiceAmount;
    private String receivedBy;
    private LocalDateTime receivedAt;

    private String rejectReason;
    private LocalDateTime createdAt;
    @Version
    private Long version;
}