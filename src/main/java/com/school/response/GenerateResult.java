package com.school.response;

import java.util.List;

public record GenerateResult(List<ExamResponse> created, List<String> unplaced) {}
