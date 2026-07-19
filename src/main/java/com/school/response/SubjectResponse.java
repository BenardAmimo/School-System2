package com.school.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubjectResponse {
    private Long subjectId;
    private String name;
    private String description;
    private String studentsFirstName;
    private String StudentsLastName;
    private String classesName;

}
