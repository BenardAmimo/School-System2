// domain/Channel.java
package com.school.money.entity;

/** How a payee wants to receive money. */
public enum Channel {
    MPESA_B2C,      // to a phone number
    MPESA_PAYBILL,  // to a business paybill + account number
    MPESA_TILL,     // to a buy-goods till
    BANK,           // bulk transfer file + recorded bank reference
    CASH,           // handed over, voucher/receipt number recorded
    CHEQUE;         // cheque number recorded

    public boolean isMpesa() { return this == MPESA_B2C || this == MPESA_PAYBILL || this == MPESA_TILL; }
    public boolean isManual() { return this == CASH || this == CHEQUE; }
}