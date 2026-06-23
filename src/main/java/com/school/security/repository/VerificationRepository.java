package com.school.security.repository;

import com.school.security.entity.UserReg;
import com.school.security.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationRepository extends JpaRepository<VerificationCode,Long> {
    Optional<VerificationCode> findByUserReg(UserReg userReg);
    Optional<VerificationCode> findByCode(String code);
}
