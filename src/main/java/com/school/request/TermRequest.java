package com.school.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TermRequest {
    @NotBlank
    private String name;
    @NotBlank
    private String year;
    @NotBlank
    private LocalDate startDate;
    @NotBlank
    private LocalDate endDate;
}
