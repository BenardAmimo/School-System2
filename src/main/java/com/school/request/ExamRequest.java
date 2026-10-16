package com.school.request;

import com.school.entity.ExamStatus;
import com.school.entity.ExamType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.time.LocalDate;
import java.time.LocalTime;

public record ExamRequest(
        @NotNull Long version,
        @NotBlank String name,
        @NotBlank LocalDate startDate,
        @NotBlank LocalDate endDate,
        @NotBlank LocalTime startTime,
        @NotBlank LocalTime endTime,
        @NotNull int totalMarks,
        @NotBlank ExamType examType,
        @NotBlank ExamStatus examStatus,
        @NotBlank String reviewComment,
        @NotNull Long assignmentId,
        @NotNull Long termId

        ) {
}
