package com.school.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreateAdHocLessonRequest {
    private Long classesId;
    private Long teacherId;
    private Long subjectId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
