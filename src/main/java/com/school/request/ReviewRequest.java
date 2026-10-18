package com.school.request;

import jakarta.validation.constraints.NotNull;

public record ReviewRequest(
        @NotNull Long version,
        boolean approve, 
        String comment) {}
