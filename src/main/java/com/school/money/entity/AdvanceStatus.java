// domain/AdvanceStatus.java
package com.school.money.entity;

public enum AdvanceStatus {
    REQUESTED,
    APPROVED /* waiting to be paid out */,
    ACTIVE /* paid out, being recovered */,
    CLOSED,
    REJECTED
}