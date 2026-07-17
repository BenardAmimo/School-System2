package com.school.payments.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;

@Data
@Validated
public class MpesaTransactionRequest {
    @NotBlank
    private String phoneNumber;
    @NotNull
    private BigDecimal amount;
    @NotBlank
    private String accountRef;
    @Size(max = 30)
    private String transactionDescription;
    @NotNull
    private Long fundsId;
}
