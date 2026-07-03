package com.school.security.service;

import com.school.security.models.*;
import jakarta.transaction.Transactional;

public interface UserServiceInterface {

    @Transactional
    String inviteUser(InviteRequest request);

    @Transactional
    RegistrationResponse completeRegistration(CompleteRegistrationRequest request);

    LoginResponse loginUser(com.school.security.models.LoginRequest loginRequest);
}
