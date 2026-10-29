package com.school.money.response;
import java.math.BigDecimal;
public record FeesResponse(BigDecimal totalFees, long payments) {}