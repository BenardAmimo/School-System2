package com.school.controller;


import com.school.request.FundsRequest;
import com.school.response.FundsResponse;
import com.school.service.FundsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class FundsController {
    private final FundsService fundsService;

    public FundsController(FundsService fundsService) {
        this.fundsService = fundsService;

    }

    @PostMapping("/funds")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<FundsResponse> createFunds(@RequestBody FundsRequest fundsRequest){
        FundsResponse respo = fundsService.createFunds(fundsRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(respo);

    }

    @GetMapping("/{studentId}/funds")
    public ResponseEntity<List<FundsResponse>> getStudentFunds(@PathVariable Long studentId) {
        return ResponseEntity.ok(fundsService.getStudentFunds(studentId));
    }
}
