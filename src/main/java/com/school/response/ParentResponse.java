package com.school.response;


import com.school.entity.Gender;
import com.school.entity.Parent;
import com.school.security.entity.UserReg;

public record ParentResponse (
        Long parentId,
        String firstName,
        String lastName,
        Gender gender,
        Long age,
        String email,
        String phoneNumber,
        boolean enabled
){
    public static ParentResponse from(Parent parent){
        UserReg user = parent.getUserReg();

        return new ParentResponse(
                parent.getParentId(),
                user.getFirstName(),
                user.getLastName(),
                parent.getGender(),
                parent.getAge(),
                user.getEmail(),
                parent.getPhoneNumber(),
                user.isEnabled()
        );

    }

}
