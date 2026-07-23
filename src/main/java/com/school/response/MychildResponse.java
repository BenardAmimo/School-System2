package com.school.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MychildResponse {
    private Long studentId;
    private String firstName;
    private String lastName;
    private String className;
    private String classLocation;
    private List<StudentSubjectSummary> subjects;
}
