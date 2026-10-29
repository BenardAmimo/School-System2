// repo/SalaryAdvanceRepository.java
package com.school.money.repository;

import com.school.money.entity.AdvanceStatus;
import com.school.money.entity.moneyEntities.SalaryAdvance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface SalaryAdvanceRepository extends JpaRepository<SalaryAdvance, Long> {
    List<SalaryAdvance> findByStaffUsernameOrderByIdDesc(String u);
    List<SalaryAdvance> findAllByOrderByIdDesc();
    List<SalaryAdvance> findByStatus(AdvanceStatus s);
    boolean existsByStaffUsernameAndStatusIn(String u, Collection<AdvanceStatus> s);
    Optional<SalaryAdvance> findFirstByStaffUsernameAndStatus(String u, AdvanceStatus s);
}