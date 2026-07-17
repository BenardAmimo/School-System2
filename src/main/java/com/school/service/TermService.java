package com.school.service;

import com.school.entity.Term;
import com.school.repo.TermRepository;
import com.school.request.TermRequest;
import com.school.response.TermResponse;
import org.springframework.stereotype.Service;

@Service
public class TermService implements TermServe{
    private final TermRepository termRepository;

    public TermService(TermRepository termRepository) {
        this.termRepository = termRepository;
    }

    @Override
    public TermResponse createNewTerm(TermRequest termRequest) {
        Term term = new Term();
        term.setName(termRequest.getName());
        term.setYear(termRequest.getYear());
        term.setStartDate(termRequest.getStartDate());
        term.setEndDate(termRequest.getEndDate());

        Term saved = termRepository.save(term);

        TermResponse termResponse = new TermResponse();
        termResponse.setTermId(saved.getTermId());
        termResponse.setName(saved.getName());
        termResponse.setYear(saved.getYear());
        termResponse.setStartDate(saved.getStartDate());
        termResponse.setEndDate(saved.getEndDate());


        return termResponse;
    }
}
