package com.school.repo;

import com.school.entity.Funds;
import com.school.entity.FundsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundsRepository extends JpaRepository<Funds, Long> {
    List<Funds> findByStudents_StudentId(Long studentId);
    boolean existsByStudents_StudentIdAndTerm_TermIdAndFundsType(Long studentId, Long termId, FundsType fundsType);
}