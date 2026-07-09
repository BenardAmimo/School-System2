package com.school.security.models;

import com.school.security.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequest{
    private String firstName;
    private String lastName;
    private String email;
    private Boolean enabled;
    private Role role;
    private String teacherNo;
    private String regNo;
}
