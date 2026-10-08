package com.school.security.repository;

import com.school.security.entity.InviteCode;
import com.school.security.entity.UserReg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface InviteTokenRepo extends JpaRepository<InviteCode,Long> {
    Optional<InviteCode> findByCode(String code);
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update InviteCode i set i.used = true where i.code = :code and i.used = false and i.expiresAt > :now")
    int claim(@Param("code") String code, @Param("now") LocalDateTime now);

    void deleteByUserReg(UserReg userReg);
}
