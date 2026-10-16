package com.school.service;

import com.school.repo.ExamRepository;
import org.springframework.stereotype.Service;

@Service
public class ExamService implements ExamServe{
    private final ExamRepository examRepository;

    public ExamService(ExamRepository examRepository) {
        this.examRepository = examRepository;
    }
}
