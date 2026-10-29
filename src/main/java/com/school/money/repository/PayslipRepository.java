// repo/PayslipRepository.java
package com.school.money.repository;

import com.school.money.entity.moneyEntities.Payslip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PayslipRepository extends JpaRepository<Payslip, Long> {
    List<Payslip> findByRunIdOrderByStaffName(Long runId);
    List<Payslip> findByStaffUsernameOrderByIdDesc(String u);

    boolean existsByAdvanceIdAndSettledFalse(Long advanceId);
}