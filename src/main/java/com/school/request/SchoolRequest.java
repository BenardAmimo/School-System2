package com.school.request;

import lombok.Data;


@Data
public class SchoolRequest {
    private String name;
    private String motto;
    private String vision;
    private Long teacherId;
    private Long studentId;
    private Long courseId;
}
