
package com.school.request;

import com.school.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

public record ParentRequest(

        @NotBlank
        @Size(max = 60)
        String firstName,

        @NotBlank @Size(max = 60)
        String lastName,

        @NotBlank
        @Email
        String email,

        @NotBlank
        String phoneNumber,

        @NotNull
        Gender gender,

        @NotNull
        @Positive
        @Max(120) Long age
) {

}
