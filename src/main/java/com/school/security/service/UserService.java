package com.school.security.service;

import com.school.entity.Parent;
import com.school.entity.Teacher;
import com.school.repo.ParentRepo;
import com.school.repo.StudentRepository;
import com.school.repo.TeacherRepo;
import com.school.security.entity.InviteCode;
import com.school.security.entity.Role;
import com.school.security.entity.UserReg;
import com.school.security.models.*;
import com.school.security.repository.InviteTokenRepo;
import com.school.security.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class UserService implements UserServiceInterface, UserDetailsService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long INVITE_DAYS = 3;
    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9._-]{3,30}$");
    // BCrypt only uses the first 72 bytes, so longer passwords add nothing.
    private static final int PASSWORD_MIN = 8, PASSWORD_MAX = 72;
    // One message for every invite failure (unknown, used, expired) so attackers learn nothing.
    private static final String INVALID_INVITE = "This invite is invalid or has expired. Ask your administrator to resend it.";

    private final UserRepository userRepo;
    private final ParentRepo parentRepo;
    private final StudentRepository studentRepository;
    private final TeacherRepo teacherRepo;
    private final PasswordEncoder passwordEncoder;
    private final InviteTokenRepo inviteTokenRepo;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserService(UserRepository userRepo, ParentRepo parentRepo, StudentRepository studentRepository,
                       TeacherRepo teacherRepo, PasswordEncoder passwordEncoder, InviteTokenRepo inviteTokenRepo,
                       EmailService emailService, JwtService jwtService,
                       @Lazy AuthenticationManager authenticationManager) {
        this.userRepo = userRepo;
        this.parentRepo = parentRepo;
        this.studentRepository = studentRepository;
        this.teacherRepo = teacherRepo;
        this.passwordEncoder = passwordEncoder;
        this.inviteTokenRepo = inviteTokenRepo;
        this.emailService = emailService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public UserDetails loadUserByUsername(@NonNull String email) throws UsernameNotFoundException {
        return userRepo.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
    }

    /* ------------------------------------------------------------------ */
    /* Invites                                                             */
    /* ------------------------------------------------------------------ */

    @Transactional
    @Override
    public String inviteUser(UserRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (email.isEmpty()) throw new IllegalArgumentException("Email is required");
        if (request.getRole() == null) throw new IllegalArgumentException("Role is required");

        if (userRepo.existsByEmail(email)) {
            throw new IllegalStateException("Email already registered!");
        }
        if (request.getRole() == Role.TEACHER && request.getTeacherNo() == null) {
            throw new IllegalArgumentException("Teacher number is required for TEACHER role");
        }
        // Consider rejecting Role.SUPER_ADMIN here unless you really want invites to create super admins.

        UserReg userReg = UserReg.builder()
                .email(email)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .enabled(false)
                .build();
        UserReg saved = userRepo.save(userReg);

        if (request.getRole() == Role.TEACHER) {
            Teacher teacher = new Teacher();
            teacher.setUserReg(saved);
            teacher.setTeacherNo(request.getTeacherNo());
            teacher.setPhoneNumber(request.getPhoneNumber());
            teacher.setAge(request.getAge());
            teacher.setGender(request.getGender());
            teacherRepo.save(teacher);
        } else if (request.getRole() == Role.PARENT) {
            Parent parent = new Parent();
            parent.setPhoneNumber(request.getPhoneNumber());
            parent.setAge(request.getAge());
            parent.setGender(request.getGender());
            parent.setUserReg(saved);
            parentRepo.save(parent);
        }

        createAndSendInvite(saved);
        return "Invite sent"; // never return the token itself
    }

    /** Replaces any outstanding invite for an account that has not been activated yet. */
    @Transactional
    @Override
    public void resendInvite(String rawEmail) {
        UserReg user = userRepo.findByEmail(normalizeEmail(rawEmail))
                .orElseThrow(() -> new IllegalArgumentException("No user with that email"));
        if (user.isEnabled()) {
            throw new IllegalStateException("This account is already active");
        }
        inviteTokenRepo.deleteByUserReg(user);
        createAndSendInvite(user);
    }

    private void createAndSendInvite(UserReg user) {
        String token = newToken();

        // Only a hash is stored: a leaked database cannot be used to activate accounts.
        InviteCode invite = InviteCode.builder()
                .code(token)//encryption
                .userReg(user)
                .used(false)
                .expiresAt(LocalDateTime.now().plusDays(INVITE_DAYS))
                .createdAt(LocalDateTime.now())
                .build();
        inviteTokenRepo.save(invite);

        // Send only after the transaction commits, so we never email a token that was rolled back.
        String email = user.getEmail();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // The email should contain a link such as https://yourapp/complete-registration?code=<token>
                emailService.sendInviteEmail(email, token);
            }
        });
    }

    @Transactional
    @Override
    public RegistrationResponse completeRegistration(CompleteRegistrationRequest request) {
        String code = request.getCode() == null ? "" : request.getCode().trim();
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();

        // Validate first so a typo does not burn the invite.
        if (code.isEmpty()) throw new IllegalArgumentException(INVALID_INVITE);
        if (!USERNAME.matcher(username).matches()) {
            throw new IllegalArgumentException("Username must be 3-30 letters, digits, dots, dashes or underscores");
        }
        if (password.length() < PASSWORD_MIN || password.getBytes(StandardCharsets.UTF_8).length > PASSWORD_MAX) {
            throw new IllegalArgumentException("Password must be " + PASSWORD_MIN + "-" + PASSWORD_MAX + " characters");
        }

        // Atomic claim: of two simultaneous requests with the same code, exactly one gets 1 here.
        String hash = sha256(code);
        if (inviteTokenRepo.claim(hash, LocalDateTime.now()) != 1) {
            throw new IllegalArgumentException(INVALID_INVITE);
        }
        InviteCode invite = inviteTokenRepo.findByCode(hash)
                .orElseThrow(() -> new IllegalArgumentException(INVALID_INVITE));

        // If this throws, the transaction rolls back and the invite is NOT consumed.
        if (userRepo.existsByUsername(username)) {
            throw new IllegalStateException("Username already taken!");
        }

        UserReg user = invite.getUserReg();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        userRepo.save(user);

        RegistrationResponse response = new RegistrationResponse();
        response.setSuccessMessage("Account activated! You can now log in.");
        return response;
    }

    /* ------------------------------------------------------------------ */
    /* Login / users                                                       */
    /* ------------------------------------------------------------------ */

    @Override
    public LoginResponse loginUser(LoginRequest loginRequest) {
        String email = normalizeEmail(loginRequest.getEmail());

        // No separate "does this email exist" lookup: that would reveal which emails are registered.
        // Wrong email, wrong password and not-yet-activated all surface as AuthenticationException.
        // Map AuthenticationException to 401 "Invalid email or password" in your @ControllerAdvice.
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword()));

        UserReg user = (UserReg) auth.getPrincipal();
        String token = jwtService.generateToken(user);

        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setRole(user.getRole().name());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setMessage("Login successful!");
        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserResponse> findAllUsers() {
        return userRepo.findAll().stream().map(this::userRespo).toList();
    }

    /** Safe for GET /me: returns a DTO, never the entity (which contains the password hash). */
    @Transactional(readOnly = true)
    @Override
    public UserResponse getCurrentUser(String email) {
        return userRepo.findByEmail(normalizeEmail(email))
                .map(this::userRespo)
                .orElseThrow(() -> new IllegalArgumentException("No user found"));
    }

    /**
     * Internal use only. Do not return this from a controller.
     */
    @Override
    public UserReg findByEmail(String name) {
        return userRepo.findByEmail(normalizeEmail(name))
                .orElseThrow(() -> new IllegalArgumentException("No user found"));
    }

    private UserResponse userRespo(UserReg userReg) {
        UserResponse userResponse = new UserResponse();
        userResponse.setUserId(userReg.getUserId());
        userResponse.setEmail(userReg.getEmail());
        userResponse.setRole(userReg.getRole());
        userResponse.setAge(userReg.getAge());
        userResponse.setGender(userReg.getGender());
        userResponse.setFirstName(userReg.getFirstName());
        userResponse.setLastName(userReg.getLastName());
        return userResponse;
    }

    /* ------------------------------------------------------------------ */
    /* Helpers                                                             */
    /* ------------------------------------------------------------------ */

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    /** 256 random bits, URL-safe. Cannot be guessed, unlike a 6-digit code. */
    private String newToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(
                    MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}