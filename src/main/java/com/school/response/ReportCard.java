package com.school.response;

import java.util.List;

public record ReportCard(Long studentId, String studentName, String className, String termName,
                         List<SubjectLine> subjects, Double totalPercent, Double averagePercent,
                         String grade, Integer position, long classSize) {}
