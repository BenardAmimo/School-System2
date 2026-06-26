package com.school.security.service;

import com.school.security.models.LoginRequest;
import com.school.security.models.LoginResponse;
import com.school.security.models.RegistrationResponse;
import com.school.security.models.UserRequest;

public interface UserServiceInterface {
    RegistrationResponse register(UserRequest request);

    String verifyUser(String code);

    LoginResponse loginUser(com.school.security.models.LoginRequest loginRequest);
}
