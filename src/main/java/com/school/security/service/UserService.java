package com.school.security.service;

import com.school.repo.SchoolRepository;
import com.school.repo.StudentRepo;
import com.school.repo.TeacherRepo;
import com.school.security.entity.InviteToken;
import com.school.security.entity.UserReg;
import com.school.security.models.*;
import com.school.security.repository.InviteTokenRepo;
import com.school.security.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserService implements UserServiceInterface , UserDetailsService {
    private final UserRepository userRepo;
    private final StudentRepo studentRepo;
    private final TeacherRepo teacherRepo;
    private final PasswordEncoder passwordEncoder;
    private final SchoolRepository schoolRepo;
    private final InviteTokenRepo inviteTokenRepo;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepo, StudentRepo studentRepo, TeacherRepo teacherRepo, PasswordEncoder passwordEncoder, SchoolRepository schoolRepo , InviteTokenRepo inviteTokenRepo, EmailService emailService, JwtService jwtService, @Lazy AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.studentRepo = studentRepo;
        this.teacherRepo = teacherRepo;
        this.passwordEncoder = passwordEncoder;
        this.schoolRepo = schoolRepo;
        this.inviteTokenRepo = inviteTokenRepo;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepo.findByEmail(email).
                orElseThrow(()->new UsernameNotFoundException("Username not found"));
    }


    @Transactional
    @Override
    public String inviteUser(InviteRequest request) {

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }

        // Created with no username/password yet — those come from the user later.
        UserReg userReg = UserReg.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .enabled(false)
                .build();

        UserReg saved = userRepo.save(userReg);

        String token = java.util.UUID.randomUUID().toString();

        InviteToken invite = InviteToken.builder()
                .token(token)
                .userReg(saved)
                .used(false)
                .expiresAt(LocalDateTime.now().plusDays(7)) // adjust window as needed
                .createdAt(LocalDateTime.now())
                .build();

        inviteTokenRepo.save(invite);

        emailService.sendInviteEmail(saved.getEmail(), token); // build this link as your frontend expects, e.g. https://yourapp.com/complete-registration?token=...

        return token;
    }

    @Transactional
    @Override
    public RegistrationResponse completeRegistration(CompleteRegistrationRequest request) {

        InviteToken invite = inviteTokenRepo.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid invite token!"));

        if (invite.isUsed()) {
            throw new RuntimeException("This invite has already been used!");
        }

        if (LocalDateTime.now().isAfter(invite.getExpiresAt())) {
            throw new RuntimeException("This invite has expired! Ask your admin to resend it.");
        }

        if (userRepo.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken!");
        }

        UserReg user = invite.getUserReg();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(true);
        userRepo.save(user);

        invite.setUsed(true);
        inviteTokenRepo.save(invite);

        RegistrationResponse response = new RegistrationResponse();
        response.setSuccessMessage("Account activated! You can now log in.");
        return response;
    }

    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {

        UserReg user = userRepo.findByEmail(loginRequest.getEmail())
                .orElseThrow(()->new RuntimeException("Email not registered"));



        authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(

                        loginRequest.getEmail(),
                        loginRequest.getPassword()

                )

        );

        String token = jwtService.generateToken(user);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRole(user.getRole().name());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setMessage("Login successful!");

        return response;
    }


}
