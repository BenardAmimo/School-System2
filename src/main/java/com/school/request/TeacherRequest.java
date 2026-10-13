
package com.school.request;

import com.school.entity.Gender;

public record TeacherRequest(
        String firstName, String lastName, String email,
        String teacherNo, String phoneNumber, Gender gender, Long age,Long userId) {

}
