package com.school.security.service;

import com.school.security.models.RegistrationResponse;
import com.school.security.models.UserRequest;
import com.school.security.models.UserResponse;
import jdk.jfr.Registered;

public interface UserServiceInterface {
    RegistrationResponse register(UserRequest request);

    String verifyUser(String code);
}
