
package com.school.request;

import com.school.entity.Gender;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class ParentRequest {
 @NotBlank
 @Size(max = 60)
 private String firstName;

 @NotBlank @Size(max = 60)
 private String lastName;

 @NotBlank @Email
 private String email;

 @NotBlank
 private String phoneNumber;

 @NotNull
 private Gender gender;

 @NotNull
 @Positive
 @Max(120) private Long age;
}
