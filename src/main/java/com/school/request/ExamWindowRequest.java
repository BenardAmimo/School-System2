package com.school.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExamWindowRequest(
@NotNull LocalDate date,
@NotNull LocalTime startTime,
@NotNull LocalTime endTime) {

}

