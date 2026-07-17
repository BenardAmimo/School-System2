package com.school.repo;

import com.school.entity.Funds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundsRepository extends JpaRepository<Funds, Long> {
    List<Funds> findByStudents_StudentId(Long studentId);
}