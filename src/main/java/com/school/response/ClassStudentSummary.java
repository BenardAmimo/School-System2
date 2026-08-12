package com.school.response;

import com.school.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassStudentSummary {
    private Long studentId;
    private String firstName;
    private String lastName;
    private Gender gender;
    private Long age;
}
