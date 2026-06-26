package com.school.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseResponse {
    private Long courseId;
    private String name;
    private String description;
    private String schoolName;
}
