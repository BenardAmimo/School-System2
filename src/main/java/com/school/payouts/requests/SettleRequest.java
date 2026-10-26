package com.school.payouts.requests;
import jakarta.validation.constraints.NotNull;
public record SettleRequest(
        @NotNull Long version,
        boolean paid,
        String reference,
        String comment) {}