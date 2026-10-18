package com.school.service;

import com.school.entity.Exam;
import com.school.entity.ExamPeriod;
import com.school.entity.ExamWindow;
import com.school.entity.Term;
import com.school.repo.ExamPeriodRepository;
import com.school.repo.ExamRepository;
import com.school.repo.TermRepository;
import com.school.request.ExamPeriodRequest;
import com.school.request.ExamWindowRequest;
import com.school.response.ExamPeriodResponse;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ExamPeriodService {

    private final ExamPeriodRepository periodRepo;
    private final ExamRepository examRepo;
    private final TermRepository termRepo;

    public ExamPeriodService(ExamPeriodRepository periodRepo, ExamRepository examRepo, TermRepository termRepo) {
        this.periodRepo = periodRepo;
        this.examRepo = examRepo;
        this.termRepo = termRepo;
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public List<ExamPeriodResponse> list() {
        return periodRepo.findAllWithTerm().stream().map(ExamPeriodResponse::from).toList();
    }

    @Transactional
    public ExamPeriodResponse create(ExamPeriodRequest r) {
        Term term = termRepo.findById(r.termId())
                .orElseThrow(() -> new IllegalArgumentException("Term not found"));
        ExamPeriod p = new ExamPeriod();
        p.setTerm(term);
        apply(p, r, term);
        return ExamPeriodResponse.from(periodRepo.save(p));
    }

    @Transactional
    public ExamPeriodResponse update(Long id, ExamPeriodRequest r) {
        ExamPeriod p = periodRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam period not found"));
        if (r.version() == null || !r.version().equals(p.getVersion()))
            throw new ObjectOptimisticLockingFailureException(ExamPeriod.class, id);
        if (!p.getTerm().getTermId().equals(r.termId()))
            throw new IllegalStateException("The term of an exam period can't be changed.");

        apply(p, r, p.getTerm());

        // existing papers must still fall inside the new dates and windows
        for (Exam e : examRepo.findByPeriodBasic(id)) {
            if (!fitsPeriod(p, e.getExamDate(), e.getStartTime(), e.getEndTime()))
                throw new IllegalStateException("\"" + e.getName() + "\" would fall outside the new dates/windows. Move it first.");
        }
        return ExamPeriodResponse.from(p);
    }

    @Transactional
    public void delete(Long id) {
        if (!examRepo.findByPeriodBasic(id).isEmpty())
            throw new IllegalStateException("Delete this period's exams first.");
        periodRepo.deleteById(id);
    }

    private void apply(ExamPeriod p, ExamPeriodRequest r, Term term) {
        if (r.endDate().isBefore(r.startDate()))
            throw new IllegalStateException("The period's end date must be on or after its start date.");
        if (r.startDate().isBefore(term.getStartDate()) || r.endDate().isAfter(term.getEndDate()))
            throw new IllegalStateException("Exam periods must fall inside the term ("
                    + term.getStartDate() + " to " + term.getEndDate() + ").");

        p.setName(r.name().trim());
        p.setExamType(r.examType());
        p.setStartDate(r.startDate());
        p.setEndDate(r.endDate());

        p.getWindows().clear();
        for (ExamWindowRequest w : r.windows()) {
            if (!w.endTime().isAfter(w.startTime()))
                throw new IllegalStateException("A window's end time must be after its start time.");
            if (w.date().isBefore(r.startDate()) || w.date().isAfter(r.endDate()))
                throw new IllegalStateException("Window on " + w.date() + " is outside the exam period.");
            ExamWindow ew = new ExamWindow();
            ew.setExamPeriod(p);
            ew.setDate(w.date());
            ew.setStartTime(w.startTime());
            ew.setEndTime(w.endTime());
            p.getWindows().add(ew);
        }
    }

    /** Shared with ExamService: date inside the period and the paper inside one window. */
    static boolean fitsPeriod(ExamPeriod p, LocalDate date, LocalTime start, LocalTime end) {
        if (date.isBefore(p.getStartDate()) || date.isAfter(p.getEndDate())) return false;
        return p.getWindows().stream().anyMatch(w ->
                w.getDate().equals(date)
                        && !start.isBefore(w.getStartTime())
                        && !end.isAfter(w.getEndTime()));
    }
}
