package com.school.payments.repository;

import com.school.payments.entity.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyKeyRepo extends JpaRepository<IdempotencyRecord,String> {
}
