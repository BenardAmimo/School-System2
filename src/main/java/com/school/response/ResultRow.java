package com.school.response;

import com.school.entity.ExamResult;
import com.school.entity.ResultStatus;
import com.school.entity.Student;

public record ResultRow(
        Long resultId, Long version, Long studentId, String studentName,
        Double marksObtained, boolean absent, String remarks,
        ResultStatus status, String reviewComment, Double percentage, String grade) {

    public static ResultRow of(Student s, ExamResult r, int totalMarks) {
        String name = s.getFirstName() + " " + s.getLastName();
        if (r == null)
            return new ResultRow(null, null, s.getStudentId(), name, null, false, null,
                    ResultStatus.DRAFT, null, null, null);
        Double pct = r.getMarksObtained() == null ? null
                : Grading.round2(r.getMarksObtained() * 100.0 / totalMarks);
        String grade = pct != null ? Grading.grade(pct) : (r.isAbsent() ? "ABS" : null);
        return new ResultRow(r.getResultId(), r.getVersion(), s.getStudentId(), name,
                r.getMarksObtained(), r.isAbsent(), r.getRemarks(),
                r.getStatus(), r.getReviewComment(), pct, grade);
    }
}

