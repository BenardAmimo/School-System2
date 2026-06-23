package com.school.security.repository;

import com.school.security.entity.UserReg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserReg,Long> {
    Optional<UserReg> findByUsername(String username);
}
