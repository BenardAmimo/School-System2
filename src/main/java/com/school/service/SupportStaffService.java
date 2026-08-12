package com.school.service;

import com.school.entity.SupportStaff;
import com.school.repo.SupportStaffRepository;
import com.school.request.SupportStaffRequest;
import com.school.response.SupportStaffResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
        staff.setAge(request.getAge());
        staff.setGender(request.getGender());
        staff.setPhoneNumber(request.getPhoneNumber());

        SupportStaff saved = staffRepository.save(staff);

        SupportStaffResponse response = new SupportStaffResponse();
        response.setStaffId(saved.getStaffId());
        response.setFirstName(saved.getFirstName());
        response.setLastName(saved.getLastName());
        response.setWorkDone(saved.getWorkDone());
        response.setAge(saved.getAge());
        response.setGender(saved.getGender());
        response.setPhoneNumber(saved.getPhoneNumber());

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

    @Override
    public SupportStaffResponse updateStaff(Long staffId, SupportStaffRequest request) {
        SupportStaff staffDB = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Support staff not found"));

        if (Objects.nonNull(request.getFirstName()) && !request.getFirstName().isBlank()) {
            staffDB.setFirstName(request.getFirstName());
        }
        if (Objects.nonNull(request.getLastName()) && !request.getLastName().isBlank()) {
            staffDB.setLastName(request.getLastName());
        }
        if (Objects.nonNull(request.getWorkDone()) && !request.getWorkDone().isBlank()) {
            staffDB.setWorkDone(request.getWorkDone());
        }

        if(Objects.nonNull(request.getAge())){//Add the and part
            staffDB.setAge(request.getAge());
        }

        if (Objects.nonNull(request.getGender())){// add the and part
            staffDB.setGender(request.getGender());
        }
        if (Objects.nonNull(request.getPhoneNumber())&& !"".equalsIgnoreCase(request.getPhoneNumber())){
            staffDB.setPhoneNumber(request.getPhoneNumber());
        }

        SupportStaff saved = staffRepository.save(staffDB);
        return mappings(saved);
    }

    @Override
    public void deleteStaffById(Long staffId) {
        SupportStaff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Support staff not found"));
        staffRepository.delete(staff);
    }

    private SupportStaffResponse mappings(SupportStaff staff){
        SupportStaffResponse resp = new SupportStaffResponse();
        resp.setStaffId(staff.getStaffId());
        resp.setFirstName(staff.getFirstName());
        resp.setLastName(staff.getLastName());
        resp.setWorkDone(staff.getWorkDone());
        resp.setAge(staff.getAge());
        resp.setGender(staff.getGender());
        resp.setPhoneNumber(staff.getPhoneNumber());

        return resp;
    }
}
