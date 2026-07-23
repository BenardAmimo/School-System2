package com.school.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchoolClassesResponse {
    private Long classesId;
    private String location;
    private String name;
    private String year;
    private int studentsCount;

    private List<ClassStudentSummary> student;
    private List<ClassSubjectSummary> subjects;
    private List<ClassTeacherSummary> teachers;
}
