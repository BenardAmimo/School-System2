package com.school.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponse {
    private Long studentId;
    private String firstName;
    private String lastName;
    private String parentFirstName;
    private String parentLastName;


}
