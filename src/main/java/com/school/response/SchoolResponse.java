package com.school.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchoolResponse {
    private Long schoolId;
    private String name;
    private String motto;
    private String vision;
    private String teacherName;
    private String courseName;
    private String studentName;
}
