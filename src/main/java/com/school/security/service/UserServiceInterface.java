package com.school.security.service;

import com.school.security.models.UserRequest;
import com.school.security.models.UserResponse;

public interface UserServiceInterface {
    UserResponse register(UserRequest request);
}
