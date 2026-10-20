package com.school.repo;

import com.school.entity.Funds;
import com.school.entity.FundsType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundsRepository extends JpaRepository<Funds, Long> {
    boolean existsByClasses_ClassesIdAndTerm_TermIdAndFundsType(Long classesId, Long termId, FundsType type);
    List<Funds> findByClasses_ClassesId(Long classesId);
}