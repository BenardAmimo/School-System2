package com.school.request;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;



public record TermRequest(
        @NotBlank
        String name,
        @NotBlank
        String year,
        @NotBlank
        LocalDate startDate,
        @NotBlank
        LocalDate endDate
) {

}
