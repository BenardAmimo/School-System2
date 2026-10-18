package com.school.response;

public record ClassRank(int position, Long studentId, String studentName,
                        double averagePercent, String grade) {}
