package com.school.money.response;

import com.school.money.entity.moneyEntities.Payslip;

import java.math.BigDecimal;
import java.util.List;

public record PayslipResponse(Long id, Long runId, String staffUsername, String staffName, BigDecimal netSalary,
                              BigDecimal fullSalary, BigDecimal advanceRecovery, BigDecimal tokenAmount, BigDecimal netPay,
                              Long paymentOrderId, boolean settled) {
    public static PayslipResponse from(Payslip e) {
        if (e == null) return null;
        return new PayslipResponse(e.getId(), e.getRunId(), e.getStaffUsername(), e.getStaffName(), e.getNetSalary(),
                e.getFullSalary(), e.getAdvanceRecovery(), e.getTokenAmount(), e.getNetPay(), e.getPaymentOrderId(), e.isSettled());
    }
    public static List<PayslipResponse> list(List<Payslip> l) { return l.stream().map(PayslipResponse::from).toList(); }
}