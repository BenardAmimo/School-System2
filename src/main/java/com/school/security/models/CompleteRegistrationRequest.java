package com.school.security.models;

import lombok.Data;

@Data
    public class CompleteRegistrationRequest {
        private String token;
        private String username;
        private String password;
    }

