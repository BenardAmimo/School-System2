package com.school.request;


import java.time.LocalDate;
import java.time.LocalTime;

public record CreateAdHocLessonRequest(
        Long classesId,
        Long teacherId,
        Long subjectId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime
) {

}
