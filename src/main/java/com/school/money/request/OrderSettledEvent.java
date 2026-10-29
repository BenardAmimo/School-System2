package com.school.money.request;


import com.school.money.entity.moneyEntities.PaymentOrder;

/** Published (inside the same transaction) when an order becomes PAID. Source modules listen to it. */
public record OrderSettledEvent(PaymentOrder order) {}