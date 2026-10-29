// domain/RefundStatus.java
package com.school.money.entity;

public enum RefundStatus {
    AWAITING_PARENT,    // credit found, parent has not chosen yet
    AWAITING_APPROVAL,  // choice made (or defaulted), admin must approve
    PAYING,             // approved refund, payment orders created
    COMPLETED,          // refunded, or carried forward
    REJECTED
}