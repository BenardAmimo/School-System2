package com.school.security.repository;

import com.school.security.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRepository extends JpaRepository<VerificationCode,Long> {
}
