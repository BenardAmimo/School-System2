package com.school.security.controller;

import com.school.security.models.*;
import com.school.security.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }


    // Admin invites a specific person with a role already assigned
    @PostMapping("/admin/invite-user")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> inviteUser(@RequestBody InviteRequest request) {
        userService.inviteUser(request);
        return ResponseEntity.ok("Invite sent");
    }


    @PostMapping("/complete-registration")
    public ResponseEntity<RegistrationResponse> completeRegistration(@RequestBody CompleteRegistrationRequest request) {
        return ResponseEntity.ok(userService.completeRegistration(request));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest loginRequest){
        LoginResponse login = userService.loginUser(loginRequest);
        return ResponseEntity.ok(login);
    }
}
