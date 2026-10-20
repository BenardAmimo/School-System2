package com.school.payments.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
@Validated
public record MpesaTransactionRequest(
        @NotBlank
        String phoneNumber,
        @NotNull
        BigDecimal amount,
        @NotBlank
        String accountRef,
        @Size(max = 30)
        String transactionDescription,
        @NotNull
        Long fundsId,
        @NotNull
        Long studentId
) {

}
