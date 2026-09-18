package com.school.service;

import com.school.request.TermRequest;
import com.school.response.TermResponse;

import java.util.List;

public interface TermServe{
    TermResponse createNewTerm(TermRequest termRequest);

    List<TermResponse> getAllTerms();

    TermResponse updateTerm(TermRequest request, Long termId);

    void deleteTerm(Long termId);
}
