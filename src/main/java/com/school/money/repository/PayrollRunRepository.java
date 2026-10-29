// repo/PayrollRunRepository.java
package com.school.money.repository;

import com.school.money.entity.moneyEntities.PayrollRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRunRepository extends JpaRepository<PayrollRun, Long> {
    Optional<PayrollRun> findByMonth(String month);
    List<PayrollRun> findAllByOrderByMonthDesc();
}