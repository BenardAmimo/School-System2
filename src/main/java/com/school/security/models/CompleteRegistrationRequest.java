package com.school.security.models;

import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
@Validated
 public record CompleteRegistrationRequest(
         @NotBlank
         String code,

         @NotBlank
         String username,

         @NotBlank
         String password ,

         @NotBlank
         String confirmPassword) {

}

