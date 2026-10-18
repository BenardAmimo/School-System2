package com.school.service;


import com.school.entity.Assignment;
import com.school.entity.Exam;
import com.school.entity.ExamPeriod;
import com.school.entity.ExamStatus;
import com.school.repo.AssignmentRepository;
import com.school.repo.ExamPeriodRepository;
import com.school.repo.ExamRepository;
import com.school.repo.ExamResultRepository;
import com.school.request.ExamRequest;
import com.school.request.ReviewRequest;
import com.school.response.ExamResponse;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ExamService implements ExamServe {

    private final ExamRepository examRepo;
    private final ExamPeriodRepository periodRepo;
    private final AssignmentRepository assignmentRepo;
    private final ExamResultRepository resultRepo;

    public ExamService(ExamRepository examRepo, ExamPeriodRepository periodRepo, AssignmentRepository assignmentRepo, ExamResultRepository resultRepo) {
        this.examRepo = examRepo;
        this.periodRepo = periodRepo;
        this.assignmentRepo = assignmentRepo;
        this.resultRepo = resultRepo;
    }

    @Transactional(readOnly = true)
    public List<ExamResponse> list(Long periodId) {
        List<Exam> exams = periodId == null
                ? examRepo.findAllWithDetails()
                : examRepo.findByPeriodWithDetails(periodId);
        return exams.stream().map(ExamResponse::from).toList();
    }

    @Transactional
    @Override
    public ExamResponse create(ExamRequest r) {
        Assignment a = assignmentRepo.findById(r.assignmentId())
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        ExamPeriod p = periodRepo.findById(r.examPeriodId())
                .orElseThrow(() -> new IllegalArgumentException("Exam period not found"));
        if (examRepo.existsForAssignment(a.getAssignmentId(), p.getExamPeriodId()))
            throw new IllegalStateException("This class already has a paper for this subject in this period.");

        validateSlot(p, a, r.examDate(), r.startTime(), r.endTime(), -1L);

        Exam e = new Exam();
        e.setAssignment(a);
        e.setExamPeriod(p);
        applyFields(e, r, a, p);
        return ExamResponse.from(examRepo.save(e));
    }

    @Transactional
    @Override
    public ExamResponse update(Long id, ExamRequest r) {
        Exam e = find(id);
        if (resultRepo.existsByExamExamId(id))
            throw new IllegalStateException("This exam already has results, so it can't be changed or deleted.");
        checkVersion(e, r.version());
        if (e.getStatus() == ExamStatus.APPROVED)
            throw new IllegalStateException("An approved exam is locked.");

        // assignment and period can't change on edit; delete and recreate instead
        validateSlot(e.getExamPeriod(), e.getAssignment(), r.examDate(), r.startTime(), r.endTime(), id);
        applyFields(e, r, e.getAssignment(), e.getExamPeriod());
        return ExamResponse.from(e);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        Exam e = find(id);
        if (resultRepo.existsByExamExamId(id))
            throw new IllegalStateException("This exam already has results, so it can't be changed or deleted.");
        if (e.getStatus() == ExamStatus.APPROVED)
            throw new IllegalStateException("An approved exam can't be deleted.");
        examRepo.delete(e);
    }

    /** Teacher (owner) or admin sends the exam for review. */
    @Transactional
    @Override
    public ExamResponse submit(Long id, Long version, Authentication auth) throws AccessDeniedException {
        Exam e = find(id);
        assertAdminOrOwner(e, auth);
        checkVersion(e, version);
        if (e.getStatus() != ExamStatus.DRAFT && e.getStatus() != ExamStatus.REJECTED)
            throw new IllegalStateException("Only draft or rejected exams can be submitted.");
        // TODO when uploads exist: require an uploaded question paper here
        e.setStatus(ExamStatus.SUBMITTED);
        e.setReviewComment(null);
        // TODO: notificationService.notifyAdmins(...)
        return ExamResponse.from(e);
    }

    /** Admin only (enforced in the controller). */
    @Transactional
    @Override
    public ExamResponse review(Long id, ReviewRequest r) {
        Exam e = find(id);
        checkVersion(e, r.version());
        // For now the admin may review from DRAFT too, since papers aren't uploaded yet.
        // TODO when uploads exist: only allow SUBMITTED.
        if (e.getStatus() != ExamStatus.DRAFT && e.getStatus() != ExamStatus.SUBMITTED)
            throw new IllegalStateException("Only draft or submitted exams can be reviewed.");
        if (!r.approve() && (r.comment() == null || r.comment().isBlank()))
            throw new IllegalStateException("Give a reason when rejecting an exam.");

        e.setStatus(r.approve() ? ExamStatus.APPROVED : ExamStatus.REJECTED);
        e.setReviewComment(r.comment() == null ? null : r.comment().trim());
        return ExamResponse.from(e);
    }

    // ---------- helpers ----------

    private void validateSlot(ExamPeriod p, Assignment a, LocalDate date,
                              LocalTime start, LocalTime end, Long excludeId) {
        if (!end.isAfter(start))
            throw new IllegalStateException("The end time must be after the start time.");
        if (!ExamPeriodService.fitsPeriod(p, date, start, end))
            throw new IllegalStateException("The paper must fall entirely inside one of the period's exam windows.");
        if (examRepo.existsClash(a.getClasses().getClassesId(), date, start, end, excludeId))
            throw new IllegalStateException(a.getClasses().getName() + " already has a paper at that time.");
    }

    private void applyFields(Exam e, ExamRequest r, Assignment a, ExamPeriod p) {
        e.setName(r.name() == null || r.name().isBlank() ? defaultName(a, p) : r.name().trim());
        e.setExamDate(r.examDate());
        e.setStartTime(r.startTime());
        e.setEndTime(r.endTime());
        e.setTotalMarks(r.totalMarks());
    }

    public static String defaultName(Assignment a, ExamPeriod p) {
        return a.getClasses().getName() + " " + a.getSubject().getName() + " "
                + p.getExamType().name().replace('_', '-').toLowerCase();
    }

    private Exam find(Long id) {
        return examRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Exam not found"));
    }

    private void checkVersion(Exam e, Long version) {
        if (version == null || !version.equals(e.getVersion()))
            throw new ObjectOptimisticLockingFailureException(Exam.class, e.getExamId());
    }

    private void assertAdminOrOwner(Exam e, Authentication auth) throws AccessDeniedException {
        boolean admin = auth.getAuthorities().stream().anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));
        String owner = e.getAssignment().getTeacher().getUserReg().getEmail();
        if (!admin && !owner.equalsIgnoreCase(auth.getName()))
            throw new AccessDeniedException("You can only submit your own exams.");
    }


}