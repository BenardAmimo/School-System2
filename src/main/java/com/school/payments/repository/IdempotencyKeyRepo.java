package com.school.payments.repository;

import com.school.payments.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface IdempotencyKeyRepo extends JpaRepository<IdempotencyRecord,String> {
    Optional<IdempotencyRecord> findByCheckoutRequestId(String checkoutRequestId);
}
