package com.school.request;

import com.school.entity.Gender;

public record SupportStaffRequest( String firstName,
                                   String lastName,
                                   String email,
                                   String workDone,
                                   Gender gender,
                                   Long userId,
                                   Long age,
                                   String phoneNumber) {


}
