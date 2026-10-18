package com.school.service;

import com.school.request.ExamRequest;
import com.school.request.ReviewRequest;
import com.school.response.ExamResponse;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface ExamServe {

    @Transactional
    ExamResponse create(ExamRequest r);

    @Transactional
    ExamResponse update(Long id, ExamRequest r);

    @Transactional
    void delete(Long id);

    @Transactional
    ExamResponse submit(Long id, Long version, Authentication auth) throws AccessDeniedException;

    @Transactional
    ExamResponse review(Long id, ReviewRequest r);

}
