package com.school.response;

import java.util.List;

public record BestStudent(String subjectName, double percent, List<String> students) {}
