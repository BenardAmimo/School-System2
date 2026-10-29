// repo/FeeCreditRepository.java
package com.school.money.repository;

import com.school.money.entity.FeeCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;

@Repository
public interface FeeCreditRepository extends JpaRepository<FeeCredit, Long> {
    @Query("select coalesce(sum(f.amount),0) from FeeCredit f where f.studentId = :sid and f.targetTermId = :tid")
    BigDecimal sumFor(@Param("sid") Long studentId, @Param("tid") Long termId);
}