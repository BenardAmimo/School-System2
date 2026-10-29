package com.school.money.contoller;

import com.school.money.request.EndEmploymentRequest;
import com.school.money.request.MonthRequest;
import com.school.money.request.SalaryRequest;
import com.school.money.response.*;
import com.school.money.service.PayrollService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/money/payroll")
public class PayrollController {

    private final PayrollService payrollService;

    public PayrollController(PayrollService payrollService) { this.payrollService = payrollService; }

    @GetMapping("/profiles")
    public ResponseEntity<List<PayProfileResponse>> profiles(Authentication auth) {
        return ResponseEntity.ok(payrollService.currentProfiles(auth));
    }

    @GetMapping("/profiles/{username}/history")
    public ResponseEntity<List<PayProfileResponse>> history(@PathVariable String username, Authentication auth) {
        return ResponseEntity.ok(payrollService.history(username, auth));
    }

    @PostMapping("/profiles")
    public ResponseEntity<PayProfileResponse> setSalary(@RequestBody SalaryRequest request, Authentication auth) {
        return ResponseEntity.ok(payrollService.setSalary(request, auth));
    }

    @PostMapping("/profiles/end")
    public ResponseEntity<PayProfileResponse> end(@RequestBody EndEmploymentRequest request, Authentication auth) {
        return ResponseEntity.ok(payrollService.endEmployment(request, auth));
    }

    @GetMapping("/runs")
    public ResponseEntity<List<PayrollRunResponse>> runs(Authentication auth) {
        return ResponseEntity.ok(payrollService.listRuns(auth));
    }

    @GetMapping("/runs/{id}/payslips")
    public ResponseEntity<List<PayslipResponse>> payslips(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(payrollService.payslips(id, auth));
    }

    @PostMapping("/runs")
    public ResponseEntity<PayrollRunResponse> prepare(@RequestBody MonthRequest request, Authentication auth) {
        return ResponseEntity.ok(payrollService.prepare(request, auth));
    }

    @PostMapping("/runs/{id}/approve")
    public ResponseEntity<PayrollRunResponse> approve(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(payrollService.approve(id, auth));
    }

    @DeleteMapping("/runs/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication auth) {
        payrollService.delete(id, auth);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-payslips")
    public ResponseEntity<List<MyPayslipResponse>> mine(Authentication auth) {
        return ResponseEntity.ok(payrollService.myPayslips(auth));
    }
}