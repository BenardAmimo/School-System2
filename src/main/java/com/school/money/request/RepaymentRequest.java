package com.school.money.request;
import java.math.BigDecimal;
public record RepaymentRequest(BigDecimal amount, String reference) {}