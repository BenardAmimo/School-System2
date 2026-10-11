package com.school.response;



import com.school.entity.TimetableSlot;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * Flat view of a weekly slot. Returning the TimetableSlot entity would serialize its whole object graph
 * (class -> teachers -> user -> parent ...) and include password hashes, as happened with lessons.
 * Types and getter names are assumed: adjust DayOfWeek/LocalTime and getId() to match your entity.
 */
public record TimetableSlotResponse(
        Long timeTableSlotId,
        Long classesId,
        String className,
        Long subjectId,
        String subjectName,
        Long teacherId,
        String teacherName,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime) {

    public static TimetableSlotResponse from( TimetableSlot s) {
        var classes = s.getClasses();
        var subject = s.getSubject();
        var teacher = s.getTeacher();
        var user = teacher == null ? null : teacher.getUserReg();

        return new TimetableSlotResponse(
                s.getTimeTableSlotId(),
                classes == null ? null : classes.getClassesId(),
                classes == null ? null : classes.getName(),
                subject == null ? null : subject.getSubjectId(),
                subject == null ? null : subject.getName(),
                teacher == null ? null : teacher.getTeacherId(),
                user == null ? null : user.getFirstName() + " " + user.getLastName(),
                s.getDayOfWeek(),
                s.getStartTime(),
                s.getEndTime());
    }
}
