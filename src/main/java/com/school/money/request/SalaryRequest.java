package com.school.money.request;
import java.math.BigDecimal;
import java.time.LocalDate;
public record SalaryRequest(String staffUsername, String staffName, BigDecimal netSalary, LocalDate effectiveFrom) {}