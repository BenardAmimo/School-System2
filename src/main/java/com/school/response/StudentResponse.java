package com.school.response;


import lombok.Data;

@Data
public class StudentResponse {
    private Long studentId;
    private String firstName;
    private String lastName;
    private String email;

}
