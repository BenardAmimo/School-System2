package com.school.payouts.requests;
import jakarta.validation.constraints.*;
public record ManualPaidRequest(
        @NotNull
        Long version,
        @NotBlank
        String reference) {}