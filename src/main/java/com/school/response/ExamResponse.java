package com.school.response;

import com.school.entity.*;
import com.school.security.entity.UserReg;

import java.time.LocalDate;
import java.time.LocalTime;

public record ExamResponse(
        Long examId, Long version, String name,
        LocalDate examDate, LocalTime startTime, LocalTime endTime, int totalMarks,
        ExamStatus status, ExamType examType, String reviewComment,
        Long assignmentId, Long examPeriodId, String examPeriodName,
        Long termId, String termName,
        String teacherName, String subjectName, String className) {

    public static ExamResponse from(Exam e) {
        Assignment a = e.getAssignment();
        ExamPeriod p = e.getExamPeriod();
        UserReg u = a.getTeacher().getUserReg();
        return new ExamResponse(
                e.getExamId(), e.getVersion(), e.getName(),
                e.getExamDate(), e.getStartTime(), e.getEndTime(), e.getTotalMarks(),
                e.getStatus(), p.getExamType(), e.getReviewComment(),
                a.getAssignmentId(), p.getExamPeriodId(), p.getName(),
                p.getTerm().getTermId(), p.getTerm().getName(),
                u.getFirstName() + " " + u.getLastName(),
                a.getSubject().getName(), a.getClasses().getName());
    }
}