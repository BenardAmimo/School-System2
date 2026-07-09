package com.school.security.service;

import com.school.security.models.*;
import jakarta.transaction.Transactional;

import java.util.List;

public interface UserServiceInterface {


    @Transactional
    String inviteUser(UserRequest request);

    @Transactional
    RegistrationResponse completeRegistration(CompleteRegistrationRequest request);

    LoginResponse loginUser(com.school.security.models.LoginRequest loginRequest);

    List<UserResponse> findAllUsers();
}
