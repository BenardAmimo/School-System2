package com.school.money.repository;

import com.school.money.entity.RefundCredit;
import com.school.money.entity.RefundStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface RefundCreditRepository extends JpaRepository<RefundCredit, Long> {

    List<RefundCredit> findAllByOrderByIdDesc();

    List<RefundCredit> findByStatus(RefundStatus s);

    List<RefundCredit> findByStatusAndDecisionDeadlineBefore(RefundStatus s, LocalDateTime t);

    Optional<RefundCredit> findByStudentIdAndTermId(Long studentId, Long termId);

    List<RefundCredit> findByStudentIdIn(Collection<Long> studentIds);

    @Query("select coalesce(sum(r.amount),0) from RefundCredit r " +
            "where r.studentId = :sid and r.termId = :tid and r.status <> :excluded")
    BigDecimal sumClaimedExcluding(@Param("sid") Long studentId,
                                   @Param("tid") Long termId,
                                   @Param("excluded") RefundStatus excluded);

    default BigDecimal sumClaimed(Long studentId, Long termId) {
        return sumClaimedExcluding(studentId, termId, RefundStatus.REJECTED);
    }
}