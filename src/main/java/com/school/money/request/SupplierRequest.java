package com.school.money.request;
public record SupplierRequest(String name, String contactPerson, String phone, String email, String kraPin, Boolean active) {}