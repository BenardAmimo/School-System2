package com.school.controller;

import com.school.entity.ExamType;
import com.school.response.BestStudent;
import com.school.response.ClassRank;
import com.school.response.ReportCard;
import com.school.service.ReportService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping("/report-card") @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public ReportCard reportCard(@RequestParam Long studentId, @RequestParam Long termId) {
        return service.reportCard(studentId, termId);
    }

    @GetMapping("/class-ranking") @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<ClassRank> ranking(@RequestParam Long classId, @RequestParam Long termId) {
        return service.classRanking(classId, termId);
    }

    @GetMapping("/best-students") @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
    public List<BestStudent> best(@RequestParam Long classId, @RequestParam Long termId,
                                  @RequestParam(required = false) ExamType examType) {
        return service.bestStudents(classId, termId, examType);
    }
}
