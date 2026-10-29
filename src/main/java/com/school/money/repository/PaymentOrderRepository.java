package com.school.money.repository;

import com.school.money.entity.Channel;
import com.school.money.entity.OrderStatus;
import com.school.money.entity.Purpose;
import com.school.money.entity.moneyEntities.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    List<PaymentOrder> findByStatusOrderByCreatedAtAsc(OrderStatus status);

    List<PaymentOrder> findAllByOrderByCreatedAtDesc();

    List<PaymentOrder> findByBatchId(String batchId);

    List<PaymentOrder> findByPurposeAndSourceType(Purpose purpose, String sourceType);

    Optional<PaymentOrder> findByOriginatorConversationId(String id);

    Optional<PaymentOrder> findByPurposeAndSourceTypeAndSourceId(Purpose p, String t, Long id);

    List<PaymentOrder> findByStatusAndSentAtBefore(OrderStatus status, LocalDateTime before);

    // ---- fees (enum passed as a parameter, not written inside the query) ----

    @Query("select coalesce(sum(o.feeCharged),0) from PaymentOrder o " +
            "where o.status = :status and o.paidAt >= :from and o.paidAt < :to")
    BigDecimal sumFeesByStatus(@Param("status") OrderStatus status,
                               @Param("from") LocalDateTime from,
                               @Param("to") LocalDateTime to);

    @Query("select count(o) from PaymentOrder o " +
            "where o.status = :status and o.feeCharged is not null " +
            "and o.paidAt >= :from and o.paidAt < :to")
    long countFeesByStatus(@Param("status") OrderStatus status,
                           @Param("from") LocalDateTime from,
                           @Param("to") LocalDateTime to);

    default BigDecimal sumFeesBetween(LocalDateTime from, LocalDateTime to) {
        return sumFeesByStatus(OrderStatus.PAID, from, to);
    }

    default long countFeedBetween(LocalDateTime from, LocalDateTime to) {
        return countFeesByStatus(OrderStatus.PAID, from, to);
    }

    // ---- amount sent since (daily-limit check) ----

    @Query("select coalesce(sum(o.amount),0) from PaymentOrder o " +
            "where o.channel not in (:excluded) and o.status in (:statuses) and o.sentAt >= :from")
    BigDecimal sumSentSinceFiltered(@Param("excluded") Collection<Channel> excluded,
                                    @Param("statuses") Collection<OrderStatus> statuses,
                                    @Param("from") LocalDateTime from);

    default BigDecimal sumSentSince(LocalDateTime from) {
        return sumSentSinceFiltered(
                List.of(Channel.CASH, Channel.CHEQUE),
                List.of(OrderStatus.SENT, OrderStatus.IN_BATCH, OrderStatus.PAID),
                from);
    }
}