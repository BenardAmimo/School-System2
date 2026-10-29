package com.school.money.contoller;

import com.school.money.response.StaffResponse;
import com.school.money.service.StaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/money/staff")
public class StaffController {

    private final StaffService staffService;

    public StaffController(StaffService staffService) { this.staffService = staffService; }

    @GetMapping
    public ResponseEntity<List<StaffResponse>> staff(Authentication auth) {
        return ResponseEntity.ok(staffService.staff(auth));
    }
}