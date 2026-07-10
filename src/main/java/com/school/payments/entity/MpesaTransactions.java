package com.school.payments.entity;

import com.school.entity.Funds;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MpesaTransactions {
    @Id
    @GeneratedValue(
            strategy = GenerationType.AUTO
    )
    private Long id;
    private String checkOutRequestId;
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
    private List<Funds> fundsList = new ArrayList<>();

}
