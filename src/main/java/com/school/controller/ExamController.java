package com.school.controller;

import com.school.request.ExamRequest;
import com.school.request.ReviewRequest;
import com.school.response.ExamResponse;
import com.school.service.ExamService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/exams")
public class ExamController {
    private final ExamService service;

    public ExamController(ExamService service) {
        this.service = service;
    }

    @GetMapping
    public List<ExamResponse> list(@RequestParam(required = false) Long periodId) { return service.list(periodId); }

    @PostMapping @PreAuthorize("hasRole('ADMIN')")
    public ExamResponse create(@Valid @RequestBody ExamRequest r) { return service.create(r); }

    @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public ExamResponse update(@PathVariable Long id, @Valid @RequestBody ExamRequest r) { return service.update(id, r); }

    @DeleteMapping("/{id}") @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) { service.delete(id); }

    @PostMapping("/{id}/submit") @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ExamResponse submit(@PathVariable Long id, @RequestParam Long version, Authentication auth) throws AccessDeniedException {
        return service.submit(id, version, auth);
    }

    @PostMapping("/{id}/review") @PreAuthorize("hasRole('ADMIN')")
    public ExamResponse review(@PathVariable Long id, @Valid @RequestBody ReviewRequest r) {
        return service.review(id, r);
    }
}