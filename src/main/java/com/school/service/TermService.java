package com.school.service;

import com.school.entity.Term;
import com.school.repo.TermRepository;
import com.school.request.TermRequest;
import com.school.response.TermResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TermService implements TermServe{
    private final TermRepository termRepository;

    public TermService(TermRepository termRepository) {
        this.termRepository = termRepository;
    }

    @Override
    public TermResponse createNewTerm(TermRequest termRequest) {
        Term term = new Term();
        term.setName(termRequest.name());
        term.setYear(termRequest.year());
        term.setStartDate(termRequest.startDate());
        term.setEndDate(termRequest.endDate());

        Term saved = termRepository.save(term);

        TermResponse termResponse = new TermResponse();
        termResponse.setTermId(saved.getTermId());
        termResponse.setName(saved.getName());
        termResponse.setYear(saved.getYear());
        termResponse.setStartDate(saved.getStartDate());
        termResponse.setEndDate(saved.getEndDate());


        return termResponse;
    }

    @Override
    public List<TermResponse> getAllTerms() {
        return termRepository
                .findAll()
                .stream()
                .map(this::toTermRespo)
                .toList();
    }

    @Override
    public TermResponse updateTerm(TermRequest request, Long termId) {

        Term termDB = termRepository.findById(termId)
                .orElseThrow(()->new RuntimeException("Term not found!"));

        if(Objects.nonNull(request.name()) && !request.name().isBlank()){
            termDB.setName(request.name());
        }

        if (Objects.nonNull(request.startDate())){//add the && part
            termDB.setStartDate(request.startDate());
        }

        if (Objects.nonNull(request.endDate())){//add the && part
            termDB.setEndDate(request.endDate());
        }

        if (Objects.nonNull(request.year()) && !request.year().isBlank()){
            termDB.setYear(request.year());
        }

        Term term = termRepository.save(termDB);

        return toTermRespo(term);
    }

    @Override
    public void deleteTerm(Long termId) {
        Term term = termRepository.findById(termId)
                .orElseThrow(()-> new RuntimeException("Term not found!"));
        termRepository.delete(term);
    }

    private TermResponse toTermRespo(Term term){
        TermResponse response = new TermResponse();
        response.setTermId(term.getTermId());
        response.setName(term.getName());
        response.setYear(term.getYear());
        response.setStartDate(term.getStartDate());
        response.setEndDate(term.getEndDate());

        return response;
    }
}
