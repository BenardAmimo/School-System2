package com.school.money.request;
import java.math.BigDecimal;
public record AdvanceRequest(BigDecimal amount, int instalments, String reason) {}