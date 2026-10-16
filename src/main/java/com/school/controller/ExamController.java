package com.school.controller;

import com.school.service.ExamService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExamController {
    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

}
