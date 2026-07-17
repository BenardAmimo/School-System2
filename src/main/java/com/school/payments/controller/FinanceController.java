package com.school.payments.controller;

import com.school.payments.service.FinanceService;
import com.school.payments.model.FinanceSummaryResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/finance")
public class FinanceController {
    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    @GetMapping("/summary")
    public ResponseEntity<FinanceSummaryResponse> getSummary() {
        return ResponseEntity.ok(financeService.getSummary());
    }
}
