package com.school.security.models;

import com.school.security.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long UserId;
    private String firstName;
    private String lastName;
    private String email;
    private String username;
    private Role role;
    private String otp; // ✅ Temporary — remove when email sending is ready
}
