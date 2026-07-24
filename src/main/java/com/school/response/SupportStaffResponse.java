package com.school.response;

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

}
