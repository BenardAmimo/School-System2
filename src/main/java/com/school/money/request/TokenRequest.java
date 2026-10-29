package com.school.money.request;
import java.math.BigDecimal;
public record TokenRequest(String staffUsername, BigDecimal amount, String reason) {}