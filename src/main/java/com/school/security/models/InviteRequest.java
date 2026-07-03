package com.school.security.models;

import com.school.security.entity.Role;
import lombok.Data;

@Data
public class InviteRequest {
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
}
