package com.school.request;

import lombok.Data;

import java.time.DayOfWeek;
import java.time.LocalTime;

@Data
public class CreateTimetableSlotRequest {
    private Long classesId;
    private Long subjectId;
    private Long teacherId;
    private DayOfWeek dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
}