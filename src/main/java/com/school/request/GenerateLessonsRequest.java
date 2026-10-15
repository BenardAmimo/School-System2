package com.school.request;


import java.time.LocalDate;

public record GenerateLessonsRequest(
        LocalDate startDate,
        LocalDate endDate
) {

}