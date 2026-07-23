package com.school.service;

import com.school.request.BulkFundsRequest;
import com.school.request.FundsRequest;
import com.school.response.FundsResponse;

public interface FundsServe {
    FundsResponse createFunds(FundsRequest fundsRequest);

    int createFundsForAllStudents(BulkFundsRequest request);
}
