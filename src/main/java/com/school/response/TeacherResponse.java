package com.school.response;

import com.school.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherResponse {
    private Long teacherId;
    private String teacherNo;
    private String firstName;
    private Gender gender;
    private Long age;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String className;
}