package com.school.controller;

import com.school.request.ReviewResultsRequest;
import com.school.request.SaveResultsRequest;
import com.school.response.ResultsResponse;
import com.school.service.ExamResultService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/exams/{examId}/results")
public class ExamResultController {
    private final ExamResultService service;

    public ExamResultController(ExamResultService service) {
        this.service = service;
    }

    @GetMapping
    public ResultsResponse sheet(@PathVariable Long examId, Authentication auth) throws AccessDeniedException {
        return service.sheet(examId, auth);
    }

    @PutMapping
    public ResultsResponse save(@PathVariable Long examId, @Valid @RequestBody SaveResultsRequest r,
                                Authentication auth) throws AccessDeniedException {
        return service.save(examId, r, auth);
    }

    @PostMapping("/submit")
    public ResultsResponse submit(@PathVariable Long examId, Authentication auth) throws AccessDeniedException {
        return service.submit(examId, auth);
    }

    @PostMapping("/review")
    public ResultsResponse review(@PathVariable Long examId, @RequestBody ReviewResultsRequest r) {
        return service.review(examId, r);
    }
}

