package com.school.security.models;

import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
 public class CompleteRegistrationRequest {
        @NotBlank
        private String code;
        @NotBlank
        private String username;
        @NotBlank
        private String password ;
        @NotBlank
        private String confirmPassword;
}

