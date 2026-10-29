package com.school.money.response;
import java.math.BigDecimal;
public record ExpenseRulesResponse(BigDecimal quotesFrom, BigDecimal tenderFrom, int minQuotes, int tenderOpenDays) {}