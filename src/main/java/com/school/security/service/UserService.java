package com.school.security.service;

import com.school.entity.School;
import com.school.entity.Student;
import com.school.entity.Teacher;
import com.school.repo.SchoolRepository;
import com.school.repo.StudentRepo;
import com.school.repo.TeacherRepo;
import com.school.security.entity.Role;
import com.school.security.entity.UserReg;
import com.school.security.entity.VerificationCode;
import com.school.security.models.RegistrationResponse;
import com.school.security.models.UserRequest;
import com.school.security.models.UserResponse;
import com.school.security.repository.UserRepository;
import com.school.security.repository.VerificationRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class UserService implements UserServiceInterface , UserDetailsService {
    private final UserRepository userRepo;
    private final StudentRepo studentRepo;
    private final TeacherRepo teacherRepo;
    private final PasswordEncoder passwordEncoder;
    private final SchoolRepository schoolRepo;
    private final VerificationRepository verificationRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepo, StudentRepo studentRepo, TeacherRepo teacherRepo, PasswordEncoder passwordEncoder, SchoolRepository schoolRepo, VerificationRepository verificationRepository, EmailService emailService) {
        this.userRepo = userRepo;
        this.studentRepo = studentRepo;
        this.teacherRepo = teacherRepo;
        this.passwordEncoder = passwordEncoder;
        this.schoolRepo = schoolRepo;
        this.verificationRepository = verificationRepository;
        this.emailService = emailService;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username).
                orElseThrow(()->new UsernameNotFoundException("Username not found"));
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }

    @Override
    public RegistrationResponse register(UserRequest request) {
        // these checks before building UserReg
        if (userRepo.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken!");
        }

        if (userRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered!");
        }

        UserReg userReg = UserReg.builder()
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .role(request.getRole())
                .enabled(false)
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        UserReg saving = userRepo.save(userReg);

        //Generates and saves OTP
        String code = generateOtp();
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setCode(code);
        verificationCode.setUserReg(saving);
        verificationCode.setExpirytime(LocalDateTime.now().plusMinutes(10));
        verificationCode.setSchoolId(request.getSchoolId());
        verificationCode.setRole(request.getRole());
        verificationCode.setTeacherNo(request.getTeacherNo());
        verificationCode.setRegNo(request.getRegNo());
        verificationRepository.save(verificationCode);


        emailService.sendEmailToUser(saving.getEmail(), "centeredproject@gmail.com",code);

        RegistrationResponse respo = new RegistrationResponse();
         respo.setSuccessMessage("Email successfully sent to your email!");

        return respo;
    }

    @Override
    @Transactional
    public String verifyUser(String code) {

            VerificationCode verification = verificationRepository.findByCode(code)
                    .orElseThrow(() -> new RuntimeException("Invalid OTP!"));

            // 2. Check expiry
            if (LocalDateTime.now().isAfter(verification.getExpirytime())) {
                verificationRepository.delete(verification);
                throw new RuntimeException("OTP has expired! Please register again.");
            }

            // 3. Enable the user
            UserReg user = verification.getUserReg();
            user.setEnabled(true);
            userRepo.save(user);

        School school = schoolRepo.findById(verification.getSchoolId())
                .orElseThrow(()->new RuntimeException("School not found"));

        // 4. Create Student or Teacher profile based on role
        if (verification.getRole() == Role.STUDENT) {

            Student student = new Student();
            student.setRegNo(verification.getRegNo());
            student.setUserReg(user);
            student.setSchool(school);

            studentRepo.save(student);

        }

        else if (verification.getRole() == Role.TEACHER) {

            Teacher teacher = new Teacher();

            teacher.setUserReg(user);
            teacher.setTeacherNo(verification.getTeacherNo());
            teacher.setSchool(school);

            teacherRepo.save(teacher);

        } else {
            throw new RuntimeException("Invalid role. Must be STUDENT or TEACHER");
        }


        // 4. Delete OTP after use
            verificationRepository.delete(verification);

            return "Account verified successfully! You can now log in.";
        }


}
