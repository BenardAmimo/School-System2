package com.school.security.models;

import com.school.security.entity.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest{
    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String email;
    @NotBlank
    private Boolean enabled;
    @NotBlank
    private Role role;
    @NotBlank
    private String teacherNo;
    @NotBlank
    private String regNo;
    @NotNull
    private Long studentId;
}
