package com.school.service;


import java.time.LocalDate;

public interface TimeTableGenerateServe {
    int generateLessons(LocalDate startDate, LocalDate endDate);
}
