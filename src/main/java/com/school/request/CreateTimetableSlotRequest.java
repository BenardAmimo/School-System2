package com.school.request;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record CreateTimetableSlotRequest(
        Long classesId,
        Long subjectId,
        Long teacherId,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime
        ) {
}