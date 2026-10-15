package com.school.response;

import com.school.entity.Gender;
import com.school.entity.SupportStaff;
import com.school.security.entity.UserReg;

public record SupportStaffResponse(
        Long staffId,
        String firstName,
        String lastName,
        String email,
        String workDone,
        Gender gender,
        Long age,
        String phoneNumber,
        boolean enabled
) {
    public static SupportStaffResponse from(SupportStaff staffs) {
        UserReg user = staffs.getUserReg();
        return new SupportStaffResponse(
                staffs.getStaffId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                staffs.getWorkDone(),
                staffs.getGender(),
                staffs.getAge(),
                staffs.getPhoneNumber(),
                user.isEnabled()
        );

    }

}
