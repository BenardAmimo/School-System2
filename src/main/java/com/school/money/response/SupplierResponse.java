package com.school.money.response;

import com.school.money.entity.moneyEntities.Supplier;

import java.util.List;

public record SupplierResponse(Long id, String name, String contactPerson, String phone, String email, String kraPin,
                               boolean active) {
    public static SupplierResponse from(Supplier e) {
        if (e == null) return null;
        return new SupplierResponse(e.getId(), e.getName(), e.getContactPerson(), e.getPhone(), e.getEmail(), e.getKraPin(), e.isActive());
    }
    public static List<SupplierResponse> list(List<Supplier> l) { return l.stream().map(SupplierResponse::from).toList(); }
}