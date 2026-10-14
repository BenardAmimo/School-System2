package com.school.response;

import com.school.entity.Gender;
import com.school.entity.Teacher;
import com.school.security.entity.UserReg;



public record TeacherResponse(
        Long teacherId,
        String teacherNo,
        String firstName,
        String lastName,
        String email,
        String phoneNumber,
        Gender gender,
        Long age,
        boolean enabled) {

    public static TeacherResponse from(Teacher teacher) {
        UserReg user = teacher.getUserReg();
        return new TeacherResponse(
                teacher.getTeacherId(),
                teacher.getTeacherNo(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                teacher.getPhoneNumber(),
                teacher.getGender(),
                teacher.getAge(),
                user.isEnabled());
    }
}