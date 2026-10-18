package com.school.controller;

import com.school.entity.ExamType;
import com.school.request.ReviewResultsRequest;
import com.school.request.SaveResultsRequest;
import com.school.response.BestStudent;
import com.school.response.ClassRank;
import com.school.response.ReportCard;
import com.school.response.ResultsResponse;
import com.school.service.ExamResultService;
import com.school.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/exams/{examId}/results")
public class ExamResultController {
    private final ExamResultService service;

    public ExamResultController(ExamResultService service) {
        this.service = service;
    }

    @GetMapping @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResultsResponse sheet(@PathVariable Long examId, Authentication auth) throws AccessDeniedException {
        return service.sheet(examId, auth);
    }

    @PutMapping @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResultsResponse save(@PathVariable Long examId, @Valid @RequestBody SaveResultsRequest r,
                                Authentication auth) throws AccessDeniedException {
        return service.save(examId, r, auth);
    }

    @PostMapping("/submit") @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ResultsResponse submit(@PathVariable Long examId, Authentication auth) throws AccessDeniedException {
        return service.submit(examId, auth);
    }

    @PostMapping("/review") @PreAuthorize("hasRole('ADMIN')")
    public ResultsResponse review(@PathVariable Long examId, @RequestBody ReviewResultsRequest r) {
        return service.review(examId, r);
    }
}

