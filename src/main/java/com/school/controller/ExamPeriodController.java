package com.school.controller;

import com.school.request.ExamPeriodRequest;
import com.school.request.GenerateRequest;
import com.school.response.ExamPeriodResponse;
import com.school.response.GenerateResult;
import com.school.service.ExamPeriodService;
import com.school.service.ExamTimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exam-periods")
@RequiredArgsConstructor
public class ExamPeriodController {
    private final ExamPeriodService service;
    private final ExamTimetableService generator;

    @GetMapping
    public List<ExamPeriodResponse> list() { return service.list(); }

    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public ExamPeriodResponse create(@Valid @RequestBody ExamPeriodRequest r) { return service.create(r); }

    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ExamPeriodResponse update(@PathVariable Long id, @Valid @RequestBody ExamPeriodRequest r) {
        return service.update(id, r);
    }

    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) { service.delete(id); }

    @PostMapping("/{id}/generate") @PreAuthorize("hasRole('ADMIN')")
    public GenerateResult generate(@PathVariable Long id, @RequestBody(required = false) GenerateRequest r) {
        return generator.generate(id, r == null ? new GenerateRequest(null, null, null, null) : r);
    }
}
