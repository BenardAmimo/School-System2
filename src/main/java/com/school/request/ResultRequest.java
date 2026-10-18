package com.school.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ResultRequest(
        @NotNull Long studentId,
        Long version,                    // null for a student with no saved row yet
        Double marksObtained,
        boolean absent,
        String remarks) {}

