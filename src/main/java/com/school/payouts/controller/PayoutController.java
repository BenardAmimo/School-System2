package com.school.payouts.controller;

import com.school.payouts.entity.PayoutStatus;
import com.school.payouts.requests.ManualPaidRequest;
import com.school.payouts.requests.PayoutDecisionRequest;
import com.school.payouts.requests.PreparePayoutRequest;
import com.school.payouts.requests.SettleRequest;
import com.school.payouts.response.PayoutResponse;
import com.school.payouts.service.PayoutSender;
import com.school.payouts.service.PayoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payouts")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class PayoutController {
    private final PayoutService service;
    private final PayoutSender sender;

    @GetMapping
    public List<PayoutResponse> list(@RequestParam(required = false) PayoutStatus status) {
        return service.list(status).stream().map(PayoutResponse::from).toList();
    }

    @PostMapping
    public PayoutResponse prepare(@Valid @RequestBody PreparePayoutRequest r, Authentication auth) {
        return PayoutResponse.from(service.prepare(r, auth));
    }

    @PostMapping("/{id}/approve")
    public PayoutResponse approve(@PathVariable Long id, @RequestParam Long version, Authentication auth) {
        return PayoutResponse.from(service.approve(id, version, auth));
    }

    @PostMapping("/{id}/reject")
    public PayoutResponse reject(@PathVariable Long id, @Valid @RequestBody PayoutDecisionRequest r, Authentication auth) {
        return PayoutResponse.from(service.reject(id, r, auth));
    }

    @PostMapping("/{id}/send")
    public PayoutResponse send(@PathVariable Long id, @RequestParam Long version) {
        return PayoutResponse.from(sender.send(id, version));
    }

    @PostMapping("/{id}/mark-paid")
    public PayoutResponse markPaid(@PathVariable Long id, @Valid @RequestBody ManualPaidRequest r, Authentication auth) {
        return PayoutResponse.from(service.markPaidManually(id, r, auth));
    }

    @PostMapping("/{id}/retry")
    public PayoutResponse retry(@PathVariable Long id, @RequestParam Long version, Authentication auth) {
        return PayoutResponse.from(service.retry(id, version, auth));
    }

    @PostMapping("/{id}/settle")
    public PayoutResponse settle(@PathVariable Long id, @Valid @RequestBody SettleRequest r, Authentication auth) {
        return PayoutResponse.from(service.settleReview(id, r, auth));
    }
}