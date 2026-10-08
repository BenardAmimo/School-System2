package com.school.controller;


import com.school.request.BulkFundsRequest;
import com.school.request.FundsRequest;
import com.school.response.FundsResponse;
import com.school.service.FundsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class FundsController {
    private final FundsService fundsService;

    public FundsController(FundsService fundsService) {
        this.fundsService = fundsService;

    }

    @PostMapping("/funds")
    public ResponseEntity<FundsResponse> createFunds(@RequestBody FundsRequest fundsRequest){
        FundsResponse respo = fundsService.createFunds(fundsRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(respo);

    }

    // FundsServe: getStudentFunds(Long studentId, Authentication authentication)
    // FundsController:
    @GetMapping("/{studentId}/funds")
    public ResponseEntity<List<FundsResponse>> getStudentFunds(
            @PathVariable Long studentId, Authentication authentication) {
        return ResponseEntity.ok(fundsService.getStudentFunds(studentId, authentication));
    }

    @PostMapping("/funds/bulk")
    public ResponseEntity<String> createFundsForAllStudents(@RequestBody BulkFundsRequest request) {
        int count = fundsService.createFundsForAllStudents(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Created fee records for " + count + " students");
    }
}
