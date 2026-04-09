package com.school.response;

import lombok.Data;

@Data
public class EnrollmentResponse {
    private Long enrollmentId;
    private String studentName;
    private String teacherName;
    private String courseName;
}
