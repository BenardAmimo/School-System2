package com.school.payouts.requests;

import com.school.payouts.entity.PayoutPurpose;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record PreparePayoutRequest(
        @NotNull PayoutPurpose purpose,
        @NotBlank String sourceType,
        @NotNull Long sourceId,
        @NotBlank String recipientName,
        @NotBlank String phoneNumber,
        @NotNull @DecimalMin("1") BigDecimal amount,
        String reason) {}