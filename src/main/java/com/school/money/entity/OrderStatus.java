// domain/OrderStatus.java
package com.school.money.entity;

public enum OrderStatus {
    APPROVED,      // approved by the business owner of the source, ready to be paid
    IN_BATCH,      // included in a bank file, waiting for the bank reference
    SENT,          // sent to M-Pesa, waiting for the result
    PAID,
    FAILED,
    NEEDS_REVIEW,  // no result after a long wait: a human must check before anything is resent
    REJECTED
}