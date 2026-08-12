package com.school.response;

import com.school.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SupportStaffResponse {
    private Long staffId;
    private String firstName;
    private String lastName;
    private String workDone;
    private Gender gender;
    private Long age;
    private String phoneNumber;

}
