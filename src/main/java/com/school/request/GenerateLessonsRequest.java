package com.school.request;


import lombok.Data;

import java.time.LocalDate;

@Data
public class GenerateLessonsRequest {
    private LocalDate startDate;
    private LocalDate endDate;
}