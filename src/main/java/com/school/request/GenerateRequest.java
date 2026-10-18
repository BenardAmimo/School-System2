package com.school.request;

public record GenerateRequest(Integer defaultDurationMinutes, Integer breakMinutes,
                              Integer maxPapersPerDay, Integer totalMarks) {
    public GenerateRequest {
        if (defaultDurationMinutes == null) defaultDurationMinutes = 60;
        if (breakMinutes == null) breakMinutes = 15;
        if (maxPapersPerDay == null) maxPapersPerDay = 3;
        if (totalMarks == null) totalMarks = 100;
    }
}
