package com.school.service;

import com.school.entity.Funds;
import com.school.entity.SchoolClasses;
import com.school.entity.Student;
import com.school.entity.Term;
import com.school.payments.entity.MpesaTransactions;
import com.school.payments.entity.Status;
import com.school.repo.FundsRepository;
import com.school.repo.SchoolClassesRepository;   // check the name of your class repository
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
    private final SchoolClassesRepository classesRepo;
    private final FundsRepository fundsRepository;
    private final TermRepository termRepository;
    private final StudentAccessPolicy accessPolicy;

    public FundsService(StudentRepository studentRepo,
                        SchoolClassesRepository classesRepo,
                        FundsRepository fundsRepository,
                        TermRepository termRepository,
                        StudentAccessPolicy accessPolicy) {
        this.studentRepo = studentRepo;
        this.classesRepo = classesRepo;
        this.fundsRepository = fundsRepository;
        this.termRepository = termRepository;
        this.accessPolicy = accessPolicy;
    }

    /** Sets a fee for a whole class. */
    @Override
    @Transactional
    public FundsResponse createFunds(FundsRequest r) {
        Funds funds = createClassFee(r.classesId(), r.termId(), r.fundsType(), r.amount());
        return toDto(funds, null);
    }

    /**
     * Same as createFunds, kept so the existing "Set Fee for Class" endpoint still compiles.
     * Returns how many students are currently in the class, for the confirmation message.
     */
    @Override
    @Transactional
    public int createFundsForAllStudents(BulkFundsRequest r) {
        createClassFee(r.classesId(), r.termId(), r.fundsType(), r.amount());
        return studentRepo.findByClasses_ClassesId(r.classesId()).size();
    }

    /**
     * Fees come from the student's class, so a child added to the class later
     * sees every existing fee straight away. Nothing needs copying per child.
     */
    @Transactional(readOnly = true)
    @Override
    public List<FundsResponse> getStudentFunds(Long studentId, Authentication authentication) {
        accessPolicy.assertCanView(studentId, authentication, false);
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student is not found"));
        if (student.getClasses() == null) {
            return List.of();
        }
        return fundsRepository.findByClasses_ClassesId(student.getClasses().getClassesId())
                .stream()
                .map(f -> toDto(f, studentId))
                .toList();
    }

    // ---------- helpers ----------

    private Funds createClassFee(Long classesId, Long termId, com.school.entity.FundsType type, BigDecimal amount) {
        requirePositive(amount);

        SchoolClasses cls = classesRepo.findById(classesId)
                .orElseThrow(() -> new IllegalArgumentException("Class is not found"));
        Term term = termRepository.findById(termId)
                .orElseThrow(() -> new IllegalArgumentException("No term found for those funds"));

        if (fundsRepository.existsByClasses_ClassesIdAndTerm_TermIdAndFundsType(
                cls.getClassesId(), term.getTermId(), type)) {
            throw new IllegalStateException("This fee already exists for the class and term");
        }

        Funds funds = new Funds();
        funds.setClasses(cls);
        funds.setTerm(term);
        funds.setFundsType(type);
        funds.setAmount(amount);
        funds.setCreatedAt(LocalDateTime.now());
        return fundsRepository.save(funds);
    }

    /**
     * Paid and balance are DERIVED from SUCCESS transactions made for THIS child.
     * studentId null (just after creating a class fee) means nothing is paid yet.
     */
    private FundsResponse toDto(Funds funds, Long studentId) {
        BigDecimal due = funds.getAmount() == null ? BigDecimal.ZERO : funds.getAmount();

        BigDecimal amountPaid = (studentId == null || funds.getMpesaTransactions() == null)
                ? BigDecimal.ZERO
                : funds.getMpesaTransactions().stream()
                .filter(t -> t.getStatus() == Status.SUCCESS)
                .filter(t -> t.getStudent() != null && studentId.equals(t.getStudent().getStudentId()))
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