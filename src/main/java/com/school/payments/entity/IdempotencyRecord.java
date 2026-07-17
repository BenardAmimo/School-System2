package com.school.payments.entity;

import jakarta.persistence.*;
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
    @Column(name = "idempotency_key", nullable = false, updatable = false)    private String idempotencyKey;
    private String checkoutRequestId;
    @Enumerated(EnumType.STRING)
    private Status status;
    private LocalDateTime createdAt;
}
