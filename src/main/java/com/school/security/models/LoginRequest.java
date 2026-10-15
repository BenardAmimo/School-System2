package com.school.security.models;

import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;


@Validated
public record LoginRequest(
        @NotBlank
        String email,

        @NotBlank
        String password
) {

}
