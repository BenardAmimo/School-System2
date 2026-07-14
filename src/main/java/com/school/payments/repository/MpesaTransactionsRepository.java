package com.school.payments.repository;

import com.school.payments.entity.MpesaTransactions;
import com.school.payments.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MpesaTransactionsRepository extends JpaRepository<MpesaTransactions,Long> {
    Optional<MpesaTransactions> findByCheckoutRequestId(String checkoutRequestId);
    List<MpesaTransactions> findByStatusAndCreatedAtBefore(Status status, LocalDateTime cutoff);
}
