// domain/TenderTier.java
package com.school.money.entity;

public enum TenderTier {
    DIRECT,   // below the first threshold: one approval
    QUOTES,   // at least three written quotes, one approval
    TENDER    // open bidding period, two-level approval (Admin then Super Admin)
}