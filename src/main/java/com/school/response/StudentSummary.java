package com.school.response;

import com.school.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentSummary {
    private Long studentId;
    private String firstName;
    private String lastName;
    private Gender gender;
    private Long age;
}
