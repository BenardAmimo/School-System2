package com.school.response;

import com.school.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassTeacherSummary {
    private Long teacherId;
    private String firstName;
    private String lastName;
    private Gender gender;
    private String phoneNumber;
    private Long age;
}
