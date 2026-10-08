package com.school.service;

import com.school.entity.Funds;
import com.school.entity.Student;
import com.school.entity.Term;
import com.school.payments.entity.MpesaTransactions;
import com.school.payments.entity.Status;
import com.school.repo.FundsRepository;
import com.school.repo.StudentRepository;
import com.school.repo.TermRepository;
import com.school.request.BulkFundsRequest;
import com.school.request.FundsRequest;
import com.school.response.FundsResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class FundsService implements FundsServe {
    private final StudentRepository studentRepo;
    private final FundsRepository fundsRepository;
    private final TermRepository termRepository;
    private final StudentAccessPolicy accessPolicy;

    public FundsService(StudentRepository studentRepo,
                        FundsRepository fundsRepository,
                        TermRepository termRepository,
                        StudentAccessPolicy accessPolicy) {
        this.studentRepo = studentRepo;
        this.fundsRepository = fundsRepository;
        this.termRepository = termRepository;
        this.accessPolicy = accessPolicy;
    }

    @Override
    @Transactional
    public FundsResponse createFunds(FundsRequest fundsRequest) {
        requirePositive(fundsRequest.getAmount());

        Student student = studentRepo.findById(fundsRequest.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student is not found"));

        Term term = termRepository.findById(fundsRequest.getTermId())
                .orElseThrow(() -> new RuntimeException("No term Found for those funds"));

        // Same duplicate rule the bulk endpoint already applies.
        if (fundsRepository.existsByStudents_StudentIdAndTerm_TermIdAndFundsType(
                student.getStudentId(), term.getTermId(), fundsRequest.getFundsType())) {
            throw new IllegalStateException("This fee already exists for the student and term");
        }

        Funds funds = new Funds();
        funds.setAmount(fundsRequest.getAmount());
        funds.setStudents(student);
        funds.setCreatedAt(LocalDateTime.now());
        funds.setTerm(term);
        funds.setFundsType(fundsRequest.getFundsType());
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
    @Transactional
    public int createFundsForAllStudents(BulkFundsRequest request) {
        requirePositive(request.getAmount());

        Term term = termRepository.findById(request.getTermId())
                .orElseThrow(() -> new RuntimeException("Term not found"));

        List<Student> students = studentRepo.findByClasses_ClassesId(request.getClassesId());

        List<Funds> newFunds = students.stream()
                .filter(student -> !fundsRepository.existsByStudents_StudentIdAndTerm_TermIdAndFundsType(
                        student.getStudentId(), term.getTermId(), request.getFundsType()))
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

    /**
     * Update FundsServe and FundsController to pass the Authentication through.
     * Without this check any logged-in parent can read any student's fees.
     */
    @Transactional(readOnly = true)
    public List<FundsResponse> getStudentFunds(Long studentId, Authentication authentication) {
        accessPolicy.assertCanView(studentId, authentication, false);
        return fundsRepository.findByStudents_StudentId(studentId).stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Paid and balance are DERIVED from SUCCESS transactions. There is no separate "credit" step,
     * so a transaction becoming SUCCESS is what marks the fee paid.
     */
    private FundsResponse toDto(Funds funds) {
        BigDecimal due = funds.getAmount() == null ? BigDecimal.ZERO : funds.getAmount();

        BigDecimal amountPaid = funds.getMpesaTransactions() == null
                ? BigDecimal.ZERO
                : funds.getMpesaTransactions().stream()
                .filter(t -> t.getStatus() == Status.SUCCESS)
                .map(MpesaTransactions::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return FundsResponse.builder()
                .fundsId(funds.getFundsId())
                .fundsType(funds.getFundsType())
                .amountDue(due)
                .amountPaid(amountPaid)
                .balance(due.subtract(amountPaid))
                .createdAt(funds.getCreatedAt())
                .termName(funds.getTerm() != null ? funds.getTerm().getName() : null)
                .termYear(funds.getTerm() != null ? funds.getTerm().getYear() : null)
                .build();
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}