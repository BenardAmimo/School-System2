package com.school.security.service;

import com.school.security.entity.UserReg;
import com.school.security.models.*;

import java.util.List;

/**
 * Transaction annotations live on the UserService implementation, not here.
 */
public interface UserServiceInterface {

    String inviteUser(UserRequest request);

    void resendInvite(String email);

    RegistrationResponse completeRegistration(CompleteRegistrationRequest request);

    LoginResponse loginUser(LoginRequest loginRequest);

    List<UserResponse> findAllUsers();

    /** Safe for controllers (GET /me): returns a DTO. */
    UserResponse getCurrentUser(String email);

    /** Internal use only. Returns the entity, which contains the password hash. Never return it from a controller. */
    UserReg findByEmail(String name);
}