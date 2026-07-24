package com.school.request;

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

}
