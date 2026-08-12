package com.school.response;


import com.school.entity.Gender;
import lombok.Data;

@Data
public class ParentResponse {
    private Long parentId;
    private String firstName;
    private String lastName;
    private Gender gender;
    private Long age;
    private String email;
    private String phoneNumber;

}
