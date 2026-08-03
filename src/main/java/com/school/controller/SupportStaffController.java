package com.school.controller;

import com.school.request.SupportStaffRequest;
import com.school.response.SupportStaffResponse;
import com.school.service.SupportStaffService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SupportStaffController {
    private final SupportStaffService staffService;

    public SupportStaffController(SupportStaffService staffService) {
        this.staffService = staffService;
    }

    @PostMapping("/staff")
    public ResponseEntity<SupportStaffResponse> createNewSupportStaff(@RequestBody SupportStaffRequest request){
        SupportStaffResponse res = staffService.createNewSupportStaff(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/staff/{staffId}")
    public ResponseEntity<SupportStaffResponse> findOneStaff(@PathVariable("staffId") Long staffId){
        SupportStaffResponse staffResponse = staffService.findOneStaff(staffId);

        return ResponseEntity.ok(staffResponse);
    }

    @GetMapping("/staffs")
    public ResponseEntity <List<SupportStaffResponse>> getAllStaffs(){
        List<SupportStaffResponse> staffs = staffService.getAllStaffs();
        return ResponseEntity.ok(staffs);
    }

    @PutMapping("staff/{staffId}")
    public ResponseEntity<SupportStaffResponse> updateStaff(
            @PathVariable Long staffId,
            @RequestBody SupportStaffRequest request) {
        return ResponseEntity.ok(staffService.updateStaff(staffId, request));
    }
    @DeleteMapping("staff/{staffId}")
    public ResponseEntity<Void> deleteStaff(@PathVariable Long staffId) {
        staffService.deleteStaffById(staffId);
        return ResponseEntity.noContent().build();
    }

}
