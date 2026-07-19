package com.school.security.service;

import com.school.entity.Parent;
import com.school.entity.Teacher;
import com.school.repo.ParentRepo;
import com.school.repo.TeacherRepo;
import com.school.security.entity.InviteCode;
import com.school.security.entity.Role;
import com.school.security.entity.UserReg;
import com.school.security.models.*;
import com.school.security.repository.InviteTokenRepo;
import com.school.security.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService implements UserServiceInterface , UserDetailsService {
    private final UserRepository userRepo;
    private final ParentRepo parentRepo;
    private final TeacherRepo teacherRepo;
    private final PasswordEncoder passwordEncoder;
    private final InviteTokenRepo inviteTokenRepo;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepo, ParentRepo parentRepo, TeacherRepo teacherRepo, PasswordEncoder passwordEncoder, InviteTokenRepo inviteTokenRepo, EmailService emailService, JwtService jwtService, @Lazy AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.parentRepo = parentRepo;
        this.teacherRepo = teacherRepo;
        this.passwordEncoder = passwordEncoder;
        this.inviteTokenRepo = inviteTokenRepo;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }
    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        return userRepo.findByEmail(email).
                orElseThrow(()->new UsernameNotFoundException("Username not found"));
    }


    public String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder(6);
        for (int i = 0; i < 6; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }


        @Transactional
        @Override
        public String inviteUser(UserRequest request){

            if (userRepo.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email already registered!");
            }

            if (request.getRole() == Role.TEACHER && request.getTeacherNo() == null) {
                throw new RuntimeException("Teacher number is required for TEACHER role");
            }
            if (request.getRole() == Role.PARENT) {
                throw new RuntimeException("Registration number is required for PARENT role");
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

            if (request.getRole() == Role.TEACHER) {
                Teacher teacher = new Teacher();
                teacher.setUserReg(userReg);
                teacher.setTeacherNo(request.getTeacherNo());

                teacherRepo.save(teacher);

            } else if (request.getRole() == Role.PARENT) {
                Parent parent = new Parent();
                parent.setUserReg(userReg);
                parentRepo.save(parent);

            } else if (request.getRole() == Role.ADMIN) {
                // no extra table needed
            }

            String code = generateOtp();

            InviteCode invite = InviteCode.builder()
                    .code(code)
                    .userReg(saved)
                    .used(false)
                    .expiresAt(LocalDateTime.now().plusDays(7)) // adjust window as needed
                    .createdAt(LocalDateTime.now())
                    .build();

            inviteTokenRepo.save(invite);

            emailService.sendInviteEmail(saved.getEmail(), code); // build this link as your frontend expects, e.g. https://yourapp.com/complete-registration?token=...

            return code;
        }

        @Transactional
        @Override
        public RegistrationResponse completeRegistration (CompleteRegistrationRequest request){

            InviteCode invite = inviteTokenRepo.findByCode(request.getCode())
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
        public LoginResponse loginUser (LoginRequest loginRequest){

            UserReg user = userRepo.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("Email not registered"));


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

    @Override
    public List<UserResponse> findAllUsers() {
        return userRepo
                .findAll()
                .stream()
                .map(this::userRespo).
                toList();
    }

    private UserResponse userRespo(UserReg userReg){

        UserResponse userResponse = new UserResponse();
        userResponse.setUserId(userReg.getUserId());
        userResponse.setEmail(userReg.getEmail());
        userResponse.setRole(userReg.getRole());
        userResponse.setFirstName(userReg.getFirstName());
        userResponse.setLastName(userReg.getLastName());

         return userResponse;


    }


}
