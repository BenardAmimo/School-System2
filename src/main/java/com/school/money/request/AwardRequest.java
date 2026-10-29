package com.school.money.request;
import java.math.BigDecimal;
public record AwardRequest(Long quoteId, Long supplierId, BigDecimal amount, String justification) {}