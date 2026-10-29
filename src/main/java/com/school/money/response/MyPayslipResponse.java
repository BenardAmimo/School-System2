package com.school.money.response;
import java.math.BigDecimal;
public record MyPayslipResponse(String month, BigDecimal netSalary, BigDecimal fullSalary, String staffName,
                                BigDecimal advanceRecovery, BigDecimal tokenAmount, BigDecimal netPay, boolean paid) {}