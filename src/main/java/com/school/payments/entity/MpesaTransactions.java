package com.school.payments.entity;

import com.school.entity.Funds;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MpesaTransactions {
    @Id
    @SequenceGenerator(
            name = "transact_gen",
            sequenceName = "transact_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "transact_gen"
    )
    private Long id;
    private String checkoutRequestId;
    private String merchantRequestId;
    private String phoneNumber;
    private BigDecimal amount;
    private String accountReference;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String mpesaReceiptNumber;
    private String resultDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @ManyToOne
    @JoinColumn(
            name = "funds_id",
            referencedColumnName = "fundsId"
    )
    private Funds funds;

}
