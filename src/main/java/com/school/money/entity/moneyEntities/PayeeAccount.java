package com.school.money.entity.moneyEntities;

import com.school.money.entity.Channel;
import com.school.money.entity.PayeeType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * How one payee wants to be paid. One row per payee.
 * Any change un-verifies the row; a second person must verify it before money can go to it.
 */
@Entity
@Table(name = "money_payee_account",
        uniqueConstraints = @UniqueConstraint(columnNames = {"payee_type", "payee_ref"}))
@Getter @Setter
public class PayeeAccount {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) @Column(name = "payee_type", nullable = false, length = 20)
    private PayeeType payeeType;

    /** STAFF: username. SUPPLIER: supplier id. PARENT: parent id. */
    @Column(name = "payee_ref", nullable = false, length = 100)
    private String payeeRef;

    @Column(nullable = false)
    private String displayName;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private Channel channel;

    /** MPESA_B2C: 2547XXXXXXXX */
    private String phone;
    /** MPESA_PAYBILL: paybill. MPESA_TILL: till number. */
    private String businessNumber;
    /** MPESA_PAYBILL: the account number the supplier gave. */
    private String accountReference;

    private String bankName;
    private String bankAccountNumber;
    private String bankAccountName;

    private boolean verified;
    private String updatedBy;
    private String verifiedBy;
    private LocalDateTime updatedAt;
    private LocalDateTime verifiedAt;

    @Version
    private Long version;
}