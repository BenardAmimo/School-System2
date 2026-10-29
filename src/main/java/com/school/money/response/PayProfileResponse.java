package com.school.money.response;

import com.school.money.entity.moneyEntities.StaffPayProfile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PayProfileResponse(Long id, String staffUsername, String staffName, BigDecimal netSalary,
                                 LocalDate effectiveFrom, String createdBy) {
    public static PayProfileResponse from(StaffPayProfile e) {
        if (e == null) return null;
        return new PayProfileResponse(e.getId(), e.getStaffUsername(), e.getStaffName(), e.getNetSalary(),
                e.getEffectiveFrom(), e.getCreatedBy());
    }
    public static List<PayProfileResponse> list(List<StaffPayProfile> l) { return l.stream().map(PayProfileResponse::from).toList(); }
}