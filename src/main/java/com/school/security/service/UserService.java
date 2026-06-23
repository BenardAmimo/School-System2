package com.school.security.service;

import com.school.entity.School;
import com.school.entity.Student;
import com.school.entity.Teacher;
import com.school.repo.SchoolRepository;
import com.school.repo.StudentRepo;
import com.school.repo.TeacherRepo;
import com.school.security.entity.Role;
import com.school.security.entity.UserReg;
import com.school.security.models.UserRequest;
import com.school.security.models.UserResponse;
import com.school.security.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class UserService implements UserServiceInterface , UserDetailsService {
    private final UserRepository userRepo;
    private final StudentRepo studentRepo;
    private final TeacherRepo teacherRepo;
    private final PasswordEncoder passwordEncoder;
    private final SchoolRepository schoolRepo;

    public UserService(UserRepository userRepo, StudentRepo studentRepo, TeacherRepo teacherRepo, PasswordEncoder passwordEncoder, SchoolRepository schoolRepo) {
        this.userRepo = userRepo;
        this.studentRepo = studentRepo;
        this.teacherRepo = teacherRepo;
        this.passwordEncoder = passwordEncoder;
        this.schoolRepo = schoolRepo;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepo.findByUsername(username).
                orElseThrow(()->new UsernameNotFoundException("Username not found"));
    }

    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Override
    public UserResponse register(UserRequest request) {

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

        School school = schoolRepo.findById(request.getSchoolId())
                .orElseThrow(()->new RuntimeException("School not found"));


        // 4. Create Student or Teacher profile based on role
        if (request.getRole() == Role.STUDENT) {

            Student student = new Student();
            student.setRegNo(request.getRegNo());
            student.setUserReg(saving);
            student.setSchool(school);

            studentRepo.save(student);

        } else if (request.getRole() == Role.TEACHER) {

            Teacher teacher = new Teacher();

            teacher.setUserReg(saving);
            teacher.setTeacherNo(request.getTeacherNo());
            teacher.setSchool(school);

            teacherRepo.save(teacher);

        } else {
            throw new RuntimeException("Invalid role. Must be STUDENT or TEACHER");
        }



        UserResponse respo = new UserResponse();
        respo.setUserId(saving.getUserId());

        return respo;
    }


}
