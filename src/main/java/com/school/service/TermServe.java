package com.school.service;

import com.school.request.TermRequest;
import com.school.response.TermResponse;

public interface TermServe{
    TermResponse createNewTerm(TermRequest termRequest);
}
