package com.school.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentSubjectSummary {
    private Long subjectId;
    private String subjectName;
    private String teacherName;
}
