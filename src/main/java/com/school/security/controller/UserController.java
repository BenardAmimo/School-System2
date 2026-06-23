package com.school.security.controller;

import com.school.security.models.UserRequest;
import com.school.security.models.UserResponse;
import com.school.security.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRequest request){
        UserResponse userRes = userService.register(request);
        return ResponseEntity.status(201).body(userRes);
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam String code) {
        return ResponseEntity.ok(userService.verifyUser(code));
    }
}
