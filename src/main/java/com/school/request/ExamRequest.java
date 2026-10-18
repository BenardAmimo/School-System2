package com.school.request;

import com.school.entity.ExamStatus;
import com.school.entity.ExamType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


import java.time.LocalDate;
import java.time.LocalTime;

public record ExamRequest(
        Long version,                                  // null on create, required on update
        String name,                                   // blank = auto-generated
        @NotNull Long assignmentId,
        @NotNull Long examPeriodId,
        @NotNull LocalDate examDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @Min(1) int totalMarks) {

}
