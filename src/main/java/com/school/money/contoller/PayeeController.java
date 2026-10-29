package com.school.money.contoller;

import com.school.money.request.PayeeRequest;
import com.school.money.response.PayeeResponse;
import com.school.money.service.PayeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/money/payees")
public class PayeeController {

    private final PayeeService payeeService;

    public PayeeController(PayeeService payeeService) { this.payeeService = payeeService; }

    @GetMapping
    public ResponseEntity<List<PayeeResponse>> list(@RequestParam(required = false) com.school.money.entity.PayeeType type, Authentication auth) {
        return ResponseEntity.ok(payeeService.list(type, auth));
    }

    @PutMapping
    public ResponseEntity<PayeeResponse> save(@RequestBody PayeeRequest request, Authentication auth) {
        return ResponseEntity.ok(payeeService.save(request, auth));
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<PayeeResponse> verify(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(payeeService.verify(id, auth));
    }
}