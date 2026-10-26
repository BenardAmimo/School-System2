package com.school.payouts.repository;

import com.school.payouts.entity.Payout;
import com.school.payouts.entity.PayoutPurpose;
import com.school.payouts.entity.PayoutStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PayoutRepository extends JpaRepository<Payout, Long> {

    boolean existsByPurposeAndSourceTypeAndSourceId(PayoutPurpose purpose, String sourceType, Long sourceId);

    Optional<Payout> findByOriginatorConversationId(String originatorConversationId);

    List<Payout> findAllByOrderByCreatedAtDesc();

    List<Payout> findByStatusOrderByCreatedAtDesc(PayoutStatus status);

    List<Payout> findByStatusAndSentAtBefore(PayoutStatus status, LocalDateTime before);

    @Query("select coalesce(sum(p.amount), 0) from Payout p " +
            "where p.status in :statuses and p.approvedAt >= :since")
    BigDecimal sumApprovedSince(@Param("statuses") Collection<PayoutStatus> statuses,
                                @Param("since") LocalDateTime since);
}