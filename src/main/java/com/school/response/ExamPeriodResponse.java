package com.school.response;

import com.school.entity.ExamPeriod;
import com.school.entity.ExamType;
import com.school.entity.ExamWindow;
import com.school.request.ExamWindowRequest;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record ExamPeriodResponse(
        Long examPeriodId, Long version, String name, ExamType examType,
        Long termId, String termName, LocalDate startDate, LocalDate endDate,
        List<ExamWindowRequest> windows) {

    public static ExamPeriodResponse from(ExamPeriod p) {
        return new ExamPeriodResponse(
                p.getExamPeriodId(), p.getVersion(), p.getName(), p.getExamType(),
                p.getTerm().getTermId(), p.getTerm().getName(),
                p.getStartDate(), p.getEndDate(),
                p.getWindows().stream()
                        .sorted(Comparator.comparing(ExamWindow::getDate).thenComparing(ExamWindow::getStartTime))
                        .map(w -> new ExamWindowRequest(w.getDate(), w.getStartTime(), w.getEndTime()))
                        .toList());
    }
}
