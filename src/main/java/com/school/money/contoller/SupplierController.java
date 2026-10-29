package com.school.money.contoller;

import com.school.money.request.SupplierRequest;
import com.school.money.response.SupplierResponse;
import com.school.money.service.SupplierService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/money/suppliers")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) { this.supplierService = supplierService; }

    @GetMapping
    public ResponseEntity<List<SupplierResponse>> list(Authentication auth) {
        return ResponseEntity.ok(supplierService.list(auth));
    }

    @PostMapping
    public ResponseEntity<SupplierResponse> create(@RequestBody SupplierRequest request, Authentication auth) {
        return ResponseEntity.ok(supplierService.create(request, auth));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponse> update(@PathVariable Long id, @RequestBody SupplierRequest request, Authentication auth) {
        return ResponseEntity.ok(supplierService.update(id, request, auth));
    }
}