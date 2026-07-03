package com.school.security.service;

import com.school.security.entity.Role;
import com.school.security.entity.UserReg;
import com.school.security.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminSeeder(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {

        if(userRepository.existsByRole(Role.SUPER_ADMIN)){
            return;
        }

        UserReg userAdmin = UserReg.builder()
                .email("centeredproject@gmail.com")
                .username("ProjectedAdmin")
                .firstName("Admin")
                .lastName("Admin")
                .password(passwordEncoder.encode("Admin@123"))
                .enabled(true)
                .role(Role.SUPER_ADMIN).
                build();

        userRepository.save(userAdmin);

    }
}
