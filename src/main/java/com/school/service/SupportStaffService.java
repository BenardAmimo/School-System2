package com.school.service;

import com.school.entity.SupportStaff;
import com.school.repo.SupportStaffRepository;
import com.school.request.SupportStaffRequest;
import com.school.response.SupportStaffResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SupportStaffService implements SupportStaffServ {
    private final SupportStaffRepository staffRepository;

    public SupportStaffService(SupportStaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @Override
    public SupportStaffResponse createNewSupportStaff(SupportStaffRequest request) {

        SupportStaff staff = new SupportStaff();
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setWorkDone(request.getWorkDone());

        SupportStaff saved = staffRepository.save(staff);

        SupportStaffResponse response = new SupportStaffResponse();
        response.setStaffId(saved.getStaffId());
        response.setFirstName(saved.getFirstName());
        response.setLastName(saved.getLastName());
        response.setWorkDone(saved.getWorkDone());

        return response;
    }

    @Override
    public SupportStaffResponse findOneStaff(Long staffId) {
         SupportStaff staff = staffRepository.findById(staffId)
                .orElseThrow(()->new RuntimeException("Staff not recorded into the System"));

        return mappings(staff);
    }

    @Override
    public List<SupportStaffResponse> getAllStaffs() {
        return staffRepository
                .findAll()
                .stream()
                .map(this::mappings)
                .toList();
    }

    private SupportStaffResponse mappings(SupportStaff staff){
        SupportStaffResponse resp = new SupportStaffResponse();
        resp.setStaffId(staff.getStaffId());
        resp.setFirstName(staff.getFirstName());
        resp.setLastName(staff.getLastName());
        resp.setWorkDone(staff.getWorkDone());

        return resp;
    }
}
