package com.school.security.repository;

import com.school.security.entity.InviteToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InviteTokenRepo extends JpaRepository<InviteToken,Long> {
    Optional<InviteToken> findByToken(String token);
}
