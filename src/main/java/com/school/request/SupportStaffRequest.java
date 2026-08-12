package com.school.request;

import com.school.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupportStaffRequest {
    private String firstName;
    private String lastName;
    private String workDone;
    private Gender gender;
    private Long age;
    private String phoneNumber;

}
