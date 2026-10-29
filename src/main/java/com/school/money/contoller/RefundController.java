package com.school.money.contoller;

import com.school.money.request.ReasonRequest;
import com.school.money.request.RefundChoiceRequest;
import com.school.money.request.ScanRequest;
import com.school.money.response.CarriedResponse;
import com.school.money.response.RefundResponse;
import com.school.money.response.ScanResponse;
import com.school.money.service.FeeCreditService;
import com.school.money.service.RefundService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/money/refunds")
public class RefundController {

    private final RefundService refundService;
    private final FeeCreditService feeCreditService;

    public RefundController(RefundService refundService, FeeCreditService feeCreditService) {
        this.refundService = refundService;
        this.feeCreditService = feeCreditService;
    }

    @PostMapping("/scan")
    public ResponseEntity<ScanResponse> scan(@RequestBody ScanRequest request, Authentication auth) {
        return ResponseEntity.ok(refundService.scan(request, auth));
    }

    @GetMapping
    public ResponseEntity<List<RefundResponse>> all(Authentication auth) {
        return ResponseEntity.ok(refundService.all(auth));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<RefundResponse>> mine(Authentication auth) {
        return ResponseEntity.ok(refundService.mine(auth));
    }

    @PostMapping("/{id}/choose")
    public ResponseEntity<RefundResponse> choose(@PathVariable Long id, @RequestBody RefundChoiceRequest request, Authentication auth) {
        return ResponseEntity.ok(refundService.choose(id, request, auth));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<RefundResponse> approve(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(refundService.approve(id, auth));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<RefundResponse> reject(@PathVariable Long id, @RequestBody ReasonRequest request, Authentication auth) {
        return ResponseEntity.ok(refundService.reject(id, request, auth));
    }

    @GetMapping("/carried/{studentId}/{termId}")
    public ResponseEntity<CarriedResponse> carried(@PathVariable Long studentId, @PathVariable Long termId, Authentication auth) {
        return ResponseEntity.ok(feeCreditService.carried(studentId, termId, auth));
    }
}