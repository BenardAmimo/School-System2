package com.school.controller;

import com.school.request.ExamPeriodRequest;
import com.school.request.GenerateRequest;
import com.school.response.ExamPeriodResponse;
import com.school.response.GenerateResult;
import com.school.service.ExamPeriodService;
import com.school.service.ExamTimetableService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @PostMapping
    public ExamPeriodResponse create(@Valid @RequestBody ExamPeriodRequest r) { return service.create(r); }

    @PutMapping("/{examPeriodId}")
    public ExamPeriodResponse update(@PathVariable Long examPeriodId, @Valid @RequestBody ExamPeriodRequest r) {
        return service.update(examPeriodId, r);
    }

    @DeleteMapping("/{examPeriodId}")
    public void delete(@PathVariable Long examPeriodId) { service.delete(examPeriodId); }

    @PostMapping("/{examPeriodId}/generate")
    public GenerateResult generate(@PathVariable Long examPeriodId, @RequestBody(required = false) GenerateRequest r) {
        return generator.generate(examPeriodId, r == null ? new GenerateRequest(null, null, null, null) : r);
    }
}
