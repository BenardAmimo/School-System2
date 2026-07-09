package com.school.security.models;

import lombok.Data;

@Data
    public class CompleteRegistrationRequest {
        private String code;
        private String username;
        private String password ;
        private String confirmPassword;
    }

