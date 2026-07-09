package com.school.security.repository;

import com.school.security.entity.InviteCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InviteTokenRepo extends JpaRepository<InviteCode,Long> {
    Optional<InviteCode> findByCode(String code);
}
