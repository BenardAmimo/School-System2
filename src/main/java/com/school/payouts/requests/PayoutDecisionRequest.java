package com.school.payouts.requests;
import jakarta.validation.constraints.NotNull;
public record PayoutDecisionRequest(
        @NotNull
        Long version,
        String comment) {}