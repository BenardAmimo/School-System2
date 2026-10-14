package com.school.service;

import com.school.response.LessonResponse;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface LessonServe {

    List<LessonResponse> getLessonsForClassOnDate(Long classesId, LocalDate date);

    List<LessonResponse> getLessonsForTeacherOnDate(Long teacherId, LocalDate date);

    LessonResponse createAdHocLesson(Long classesId, Long teacherId, Long subjectId, LocalDate date, LocalTime startTime, LocalTime endTime);
}
