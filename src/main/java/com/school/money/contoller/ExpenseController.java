package com.school.money.contoller;

import com.school.money.request.*;
import com.school.money.response.ExpenseResponse;
import com.school.money.response.ExpenseRulesResponse;
import com.school.money.response.QuoteResponse;
import com.school.money.service.ExpenseService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/money/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> list(Authentication auth) {
        return ResponseEntity.ok(expenseService.list(auth));
    }

    @GetMapping("/rules")
    public ResponseEntity<ExpenseRulesResponse> rules(Authentication auth) {
        return ResponseEntity.ok(expenseService.rules(auth));
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> create(@RequestBody ExpenseRequest request, Authentication auth) {
        return ResponseEntity.ok(expenseService.create(request, auth));
    }

    @GetMapping("/{id}/quotes")
    public ResponseEntity<List<QuoteResponse>> quotes(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(expenseService.quotesOf(id, auth));
    }

    @PostMapping("/{id}/quotes")
    public ResponseEntity<QuoteResponse> addQuote(@PathVariable Long id,
                                                  @RequestBody QuoteRequest request,
                                                  Authentication auth) {
        return ResponseEntity.ok(expenseService.addQuote(id, request, auth));
    }

    @PostMapping("/{id}/award")
    public ResponseEntity<ExpenseResponse> award(@PathVariable Long id,
                                                 @RequestBody AwardRequest request,
                                                 Authentication auth) {
        return ResponseEntity.ok(expenseService.award(id, request, auth));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ExpenseResponse> approve(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(expenseService.approve(id, auth));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ExpenseResponse> reject(@PathVariable Long id,
                                                  @RequestBody ReasonRequest request,
                                                  Authentication auth) {
        return ResponseEntity.ok(expenseService.reject(id, request, auth));
    }

    @PostMapping("/{id}/receive")
    public ResponseEntity<ExpenseResponse> receive(@PathVariable Long id,
                                                   @RequestBody ReceiptRequest request,
                                                   Authentication auth) {
        return ResponseEntity.ok(expenseService.receive(id, request, auth));
    }

    @PostMapping("/{id}/order")
    public ResponseEntity<ExpenseResponse> order(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(expenseService.order(id, auth));
    }
}