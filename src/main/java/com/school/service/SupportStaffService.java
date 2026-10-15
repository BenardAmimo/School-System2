package com.school.service;

import com.school.entity.SupportStaff;
import com.school.repo.SupportStaffRepository;
import com.school.request.SupportStaffRequest;
import com.school.response.SubjectResponse;
import com.school.response.SupportStaffResponse;
import com.school.security.entity.UserReg;
import com.school.security.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class SupportStaffService implements SupportStaffServ {
    private final SupportStaffRepository staffRepository;
    private final UserRepository userRepository;

    public SupportStaffService(SupportStaffRepository staffRepository, UserRepository userRepository) {
        this.staffRepository = staffRepository;
        this.userRepository = userRepository;
    }


    @Override
    public SupportStaffResponse findOneStaff(Long staffId) {
         SupportStaff staff = staffRepository.findById(staffId)
                .orElseThrow(()->new RuntimeException("Staff not recorded into the System"));

        return SupportStaffResponse.from(staff);
    }

    @Override
    public List<SupportStaffResponse> getAllStaffs() {
        return staffRepository
                .findAll()
                .stream()
                .map(SupportStaffResponse::from)
                .toList();
    }

    @Override
    public SupportStaffResponse updateStaff(Long staffId, SupportStaffRequest staffRequest) {
        SupportStaff staffDB = staffRepository.findById(staffId)
                .orElseThrow();

        if (Objects.nonNull(staffRequest.workDone()) && !staffRequest.workDone().isBlank()) {
            staffDB.setWorkDone(staffRequest.workDone());
        }

        if (Objects.nonNull(staffRequest.phoneNumber()) && !staffRequest.phoneNumber().isBlank()) {
            staffDB.setPhoneNumber(staffRequest.phoneNumber());
        }
        if (Objects.nonNull(staffRequest.age())){
            staffDB.setAge(staffRequest.age());
        }

        if (Objects.nonNull(staffRequest.userId())) {
            UserReg user = userRepository.findById(staffRequest.userId())
                    .orElseThrow(() -> new RuntimeException("User not found!"));
            staffDB.setUserReg(user);
        }

        SupportStaff saved = staffRepository.save(staffDB);
        return SupportStaffResponse.from(saved);
    }

    @Override
    public void deleteStaffById(Long staffId) {
        SupportStaff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("Support staff not found"));
        staffRepository.delete(staff);
    }


}
