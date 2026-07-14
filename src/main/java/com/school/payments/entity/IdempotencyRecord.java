package com.school.payments.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdempotencyRecord {
    @Id
    @GeneratedValue(
            strategy = GenerationType.AUTO
    )
    private String idempotencyKey;
    private String checkoutRequestId;
    private Status status;
    private LocalDateTime createdAt;
}
