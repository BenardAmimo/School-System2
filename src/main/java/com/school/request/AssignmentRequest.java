package com.school.request;

public record AssignmentRequest(
        Long teacherId,
        Long subjectId,
        Long classesId
) {
}
