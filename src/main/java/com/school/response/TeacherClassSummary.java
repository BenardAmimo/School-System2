package com.school.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherClassSummary {
    private Long assignmentId;
    private String subjectName;
    private String className;
    private String classLocation;
    private List<StudentSummary> students;
}
