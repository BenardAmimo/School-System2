package com.school.response;

import com.school.entity.Lesson;

import java.time.LocalDate;
import java.time.LocalTime;

public record LessonResponse(
        Long lessonId,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        Long classesId,
        String className,
        Long subjectId,
        String subjectName,
        Long teacherId,
        String teacherName,
        boolean adHoc) {

    public static LessonResponse from(Lesson l) {
        var classes = l.getClasses();
        var subject = l.getSubject();
        var teacher = l.getTeacher();
        var user = teacher == null ? null : teacher.getUserReg();

        return new LessonResponse(
                l.getLessonId(),
                l.getDate(),
                l.getStartTime(),
                l.getEndTime(),
                classes == null ? null : classes.getClassesId(),
                classes == null ? null : classes.getName(),
                subject == null ? null : subject.getSubjectId(),
                subject == null ? null : subject.getName(),
                teacher == null ? null : teacher.getTeacherId(),
                user == null ? null : (user.getFirstName() + " " + user.getLastName()).trim(),
                l.getTimetableSlot() == null);
    }
}
