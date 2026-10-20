package com.school.service;

import com.school.request.BulkFundsRequest;
import com.school.request.FundsRequest;
import com.school.response.FundsResponse;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface FundsServe {
    FundsResponse createFunds(FundsRequest fundsRequest);

    int createFundsForAllStudents(BulkFundsRequest request);

    @Transactional(readOnly = true)
    List<FundsResponse> getStudentFunds(Long studentId, Authentication authentication);
}
