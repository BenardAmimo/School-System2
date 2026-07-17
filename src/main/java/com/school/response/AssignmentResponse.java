package com.school.response;

import lombok.Data;

@Data
public class AssignmentResponse {
    private Long assignmentId;
    private String subjectName;
    private String teacherName;
}
