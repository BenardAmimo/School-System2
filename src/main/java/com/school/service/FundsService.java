package com.school.service;

import com.school.entity.*;
import com.school.payments.entity.MpesaTransactions;
import com.school.payments.entity.Status;
import com.school.repo.FundsRepository;
import com.school.repo.ParentRepo;
import com.school.repo.StudentRepository;
import com.school.repo.TermRepository;
import com.school.request.BulkFundsRequest;
import com.school.request.FundsRequest;
import com.school.response.FundsResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FundsService implements FundsServe{
    private final StudentRepository studentRepo;
    private final FundsRepository fundsRepository;
    private final TermRepository termRepository;

    public FundsService(StudentRepository studentRepo, FundsRepository fundsRepository, TermRepository termRepository) {
        this.studentRepo = studentRepo;
        this.fundsRepository = fundsRepository;
        this.termRepository = termRepository;
    }

    @Override
    public FundsResponse createFunds(FundsRequest fundsRequest) {
        Student student = studentRepo.findById(fundsRequest.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student is not found"));

        Term term = termRepository.findById(fundsRequest.getTermId())
                .orElseThrow(()->new RuntimeException("No term Found for those funds"));

        Funds funds = new Funds();
        funds.setAmount(fundsRequest.getAmount());
        funds.setStudents(student);
        funds.setCreatedAt(LocalDateTime.now());
        funds.setTerm(term);
        funds.setFundsType(fundsRequest.getFundsType());//to be fixed
        fundsRepository.save(funds);

        return FundsResponse.builder()
                .fundsId(funds.getFundsId())
                .fundsType(funds.getFundsType())
                .amountDue(funds.getAmount())
                .createdAt(funds.getCreatedAt())
                .termName(funds.getTerm().getName())
                .termYear(funds.getTerm().getYear())
                .build();
    }

    @Override
    public int createFundsForAllStudents(BulkFundsRequest request) {
        Term term = termRepository.findById(request.getTermId())
                .orElseThrow(() -> new RuntimeException("Term not found"));

        List<Student> allStudents = studentRepo.findAll();

        List<Funds> newFunds = allStudents.stream()
                .map(student -> {
                    Funds funds = new Funds();
                    funds.setStudents(student);
                    funds.setTerm(term);
                    funds.setFundsType(request.getFundsType());
                    funds.setAmount(request.getAmount());
                    funds.setCreatedAt(LocalDateTime.now());
                    return funds;
                })
                .toList();

        fundsRepository.saveAll(newFunds);
        return newFunds.size();
    }

    public List<FundsResponse> getStudentFunds(Long studentId) {
            return fundsRepository.findByStudents_StudentId(studentId).stream()
                    .map(this::toDto)
                    .toList();
        }

        private FundsResponse toDto(Funds funds) {
            BigDecimal amountPaid = funds.getMpesaTransactions() == null
                    ? BigDecimal.ZERO
                    : funds.getMpesaTransactions().stream()
                    .filter(t -> t.getStatus() == Status.SUCCESS)
                    .map(MpesaTransactions::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return FundsResponse.builder()
                    .fundsId(funds.getFundsId())
                    .fundsType(funds.getFundsType())
                    .amountDue(funds.getAmount())
                    .amountPaid(amountPaid)
                    .balance(funds.getAmount().subtract(amountPaid))
                    .createdAt(funds.getCreatedAt())
                    .termName(funds.getTerm() != null ? funds.getTerm().getName() : null)
                    .termYear(funds.getTerm() != null ? funds.getTerm().getYear() : null)
                    .build();
        }
    }

