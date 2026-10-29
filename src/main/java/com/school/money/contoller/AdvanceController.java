package com.school.money.contoller;

import com.school.money.request.AdvanceRequest;
import com.school.money.request.ReasonRequest;
import com.school.money.request.RepaymentRequest;
import com.school.money.response.AdvanceResponse;
import com.school.money.response.AdvanceSummaryResponse;
import com.school.money.service.AdvanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/money/advances")
public class AdvanceController {

    private final AdvanceService advanceService;

    public AdvanceController(AdvanceService advanceService) { this.advanceService = advanceService; }

    @PostMapping
    public ResponseEntity<AdvanceResponse> request(@RequestBody AdvanceRequest request, Authentication auth) {
        return ResponseEntity.accepted().body(advanceService.request(request, auth));
    }

    @GetMapping("/mine")
    public ResponseEntity<AdvanceSummaryResponse> mine(Authentication auth) {
        return ResponseEntity.ok(advanceService.mine(auth));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<AdvanceResponse> cancel(@PathVariable Long id, Authentication auth) {
        AdvanceResponse resp = advanceService.cancel(id,auth);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<List<AdvanceResponse>> all(Authentication auth) {
        return ResponseEntity.ok(advanceService.all(auth));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<AdvanceResponse> approve(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(advanceService.approve(id, auth));
    }

    @PostMapping("/{id}/repay")
    public ResponseEntity<AdvanceResponse> repay(@PathVariable Long id, @RequestBody RepaymentRequest request, Authentication auth) {
        return ResponseEntity.ok(advanceService.recordRepayment(id, request, auth));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<AdvanceResponse> reject(@PathVariable Long id, @RequestBody ReasonRequest request, Authentication auth) {
        return ResponseEntity.ok(advanceService.reject(id, request, auth));
    }
}