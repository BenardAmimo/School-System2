package com.school.money.request;
import java.math.BigDecimal;
public record ExpenseRequest(String title, String description, String category, BigDecimal estimatedAmount) {}