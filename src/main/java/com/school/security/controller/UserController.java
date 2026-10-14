package com.school.security.controller;
import com.school.security.models.*;
import com.school.security.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class

UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/admin/invite-user")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> inviteUser(@RequestBody UserRequest request) {
        userService.inviteUser(request);
        return ResponseEntity.ok("Invite sent");
    }


    @PostMapping("/complete-registration")
    public ResponseEntity<RegistrationResponse> completeRegistration(@RequestBody CompleteRegistrationRequest request) {
        return ResponseEntity.ok(userService.completeRegistration(request));
    }

    @PostMapping("/invite-valid")
    public ResponseEntity<java.util.Map<String, Boolean>> inviteValid(@RequestBody InviteCheckRequest request) {
        return ResponseEntity.ok(java.util.Map.of("valid", userService.isInviteValid(request.code())));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@RequestBody LoginRequest loginRequest){
        System.out.println("LOGIN CONTROLLER");
        System.out.println("Email" + loginRequest.email());
        LoginResponse login = userService.loginUser(loginRequest);
        return ResponseEntity.ok(login);
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> findAllUsers(){
        List<UserResponse> allUsers = userService.findAllUsers();
        return ResponseEntity.ok(allUsers);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        return ResponseEntity.ok(userService.getCurrentUser(authentication.getName()));
    }

    public record ResendInviteRequest(String email) {}

    @PostMapping("/admin/resend-invite")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<String> resendInvite(@RequestBody ResendInviteRequest request) {
        userService.resendInvite(request.email());
        return ResponseEntity.ok("Invite resent");
    }
}
