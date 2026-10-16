package com.school.response;

import com.school.entity.*;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExamResponse(
        Long examId,
        Long version,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        LocalTime startTime,
        LocalTime endTime,
        int totalMarks,
        ExamStatus examStatus,
        ExamType examType,
        String reviewComment,
        String teacherName,
        String subjectName,
        String className,
        String termName

){
    public static ExamResponse from(Exam exams) {
        Assignment a = exams.getAssignment();
        Teacher teacher = a.getTeacher();

        return new ExamResponse(
                exams.getExamId(),
                exams.getVersion(),
                exams.getName(),
                exams.getStartDate(),
                exams.getEndDate(),
                exams.getStartTime(),
                exams.getEndTime(),
                exams.getTotalMarks(),
                exams.getStatus(),
                exams.getExamType(),
                exams.getReviewComment(),
                teacher.getUserReg().getFirstName() + " " + teacher.getUserReg().getLastName(),
                a.getSubject().getName(),
                a.getClasses().getName(),
                exams.getTerm().getName()
        );
    }
}
