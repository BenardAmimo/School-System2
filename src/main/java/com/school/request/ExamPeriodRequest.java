package com.school.request;

import com.school.entity.ExamType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record ExamPeriodRequest (
        Long version,                                  // null on create
        @NotBlank
        String name,
        @NotNull
        ExamType examType,
        @NotNull Long termId,
        @NotNull LocalDate startDate,
        @NotNull
        LocalDate endDate,
        @NotNull
        List<@Valid ExamWindowRequest> windows) {

        }

