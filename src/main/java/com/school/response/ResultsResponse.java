package com.school.response;

import java.util.List;

public record ResultsResponse(
        Long examId, String examName, String className, String subjectName,
        int totalMarks, String resultsStatus, List<ResultRow> rows) {}
