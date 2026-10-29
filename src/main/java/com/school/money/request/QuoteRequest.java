package com.school.money.request;
import java.math.BigDecimal;
public record QuoteRequest(Long supplierId, BigDecimal amount, String notes) {}