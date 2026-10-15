package com.school.security.models;

import com.school.entity.Gender;
import com.school.security.entity.Role;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;

public record UserRequest(
        @NotBlank
        String firstName,

        @NotBlank
        String lastName,

        @NotBlank
        String email,

        @NotBlank
        boolean enabled,

        @NotBlank
        Role role,

        @NotBlank
        String teacherNo,

        @NotBlank
        String phoneNumber,

        @NotBlank
        Gender gender,

        @NonNull
        Long age,

        @NotBlank
        String workDone

){

}
