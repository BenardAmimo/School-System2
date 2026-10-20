package com.school.controller;

import com.school.request.ExamRequest;
import com.school.request.ReviewRequest;
import com.school.response.ExamResponse;
import com.school.service.ExamService;
import jakarta.validation.Valid;
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

    @PostMapping
    public ExamResponse create(@Valid @RequestBody ExamRequest r) { return service.create(r); }

    @PutMapping("/{examId}")
    public ExamResponse update(@PathVariable Long examId, @Valid @RequestBody ExamRequest r) { return service.update(examId, r); }

    @DeleteMapping("/{examId}")
    public void delete(@PathVariable Long examId) { service.delete(examId); }

    @PostMapping("/{examId}/submit")
    public ExamResponse submit(@PathVariable Long examId, @RequestParam Long version, Authentication auth) throws AccessDeniedException {
        return service.submit(examId, version, auth);
    }

    @PostMapping("/{examId}/review")
    public ExamResponse review(@PathVariable Long examId, @Valid @RequestBody ReviewRequest r) {
        return service.review(examId, r);
    }
}