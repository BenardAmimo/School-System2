package com.school.service;

import com.school.request.SupportStaffRequest;
import com.school.response.SupportStaffResponse;

import java.util.List;

public interface SupportStaffServ {

    SupportStaffResponse findOneStaff(Long staffId);

    List<SupportStaffResponse> getAllStaffs();

    SupportStaffResponse updateStaff(Long staffId, SupportStaffRequest staffRequest);

    void deleteStaffById(Long staffId);
}
