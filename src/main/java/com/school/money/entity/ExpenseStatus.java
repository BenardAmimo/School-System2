// domain/ExpenseStatus.java
package com.school.money.entity;

public enum ExpenseStatus {
    REQUESTED,          // requisition raised, collecting quotes / bids if the tier needs them
    AWAITING_APPROVAL,  // supplier chosen, waiting for approval(s)
    APPROVED,           // approved, goods/service not yet received
    RECEIVED,           // delivery and invoice recorded, matched
    PAYMENT_ORDERED,    // payment order created
    PAID,
    REJECTED
}