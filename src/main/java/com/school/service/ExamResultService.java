package com.school.service;

import com.school.entity.*;
import com.school.repo.ExamRepository;
import com.school.repo.ExamResultRepository;
import com.school.repo.StudentRepository;
import com.school.request.ResultRequest;
import com.school.request.ReviewResultsRequest;
import com.school.request.SaveResultsRequest;
import com.school.response.ResultRow;
import com.school.response.ResultsResponse;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ExamResultService {

    private final ExamRepository examRepo;
    private final ExamResultRepository resultRepo;
    private final StudentRepository studentRepo;

    private static final ZoneId NAIROBI = ZoneId.of("Africa/Nairobi");

    public ExamResultService(ExamRepository examRepo, ExamResultRepository resultRepo, StudentRepository studentRepo) {
        this.examRepo = examRepo;
        this.resultRepo = resultRepo;
        this.studentRepo = studentRepo;
    }

    @Transactional(readOnly = true)
    public ResultsResponse sheet(Long examId, Authentication auth) throws AccessDeniedException {
        Exam exam = load(examId);
        assertAdminOrOwner(exam, auth);
        return build(exam, resultRepo.findByExamWithStudents(examId), roster(exam));
    }

    /** Teacher (owner) or admin saves marks. Only DRAFT/REJECTED rows can change. */
    @Transactional
    public ResultsResponse save(Long examId, SaveResultsRequest req, Authentication auth) throws AccessDeniedException {
        Exam exam = load(examId);
        assertAdminOrOwner(exam, auth);
        if (exam.getStatus() != ExamStatus.APPROVED)
            throw new IllegalStateException("Marks can only be entered for an approved exam.");
        if (exam.getExamDate().isAfter(LocalDate.now(NAIROBI)))
            throw new IllegalStateException("Marks can be entered once the exam has been sat.");

        List<Student> roster = roster(exam);
        Map<Long, Student> students = roster.stream()
                .collect(Collectors.toMap(Student::getStudentId, s -> s));
        Map<Long, ExamResult> existing = resultRepo.findByExamWithStudents(examId).stream()
                .collect(Collectors.toMap(r -> r.getStudent().getStudentId(), r -> r));

        for (ResultRequest en : req.results()) {
            Student st = students.get(en.studentId());
            if (st == null)
                throw new IllegalStateException("Student " + en.studentId() + " is not in "
                        + exam.getAssignment().getClasses().getName() + ".");
            String who = st.getFirstName() + " " + st.getLastName();

            if (en.absent()) {
                if (en.marksObtained() != null)
                    throw new IllegalStateException(who + " is marked absent, so they can't have marks.");
            } else if (en.marksObtained() != null
                    && (en.marksObtained() < 0 || en.marksObtained() > exam.getTotalMarks())) {
                throw new IllegalStateException("Marks for " + who + " must be between 0 and "
                        + exam.getTotalMarks() + ".");
            }

            ExamResult r = existing.get(en.studentId());
            if (r == null) {
                r = new ExamResult();
                r.setExam(exam);
                r.setStudent(st);
            } else {
                if (r.getStatus() != ResultStatus.DRAFT && r.getStatus() != ResultStatus.REJECTED)
                    throw new IllegalStateException(who + "'s result is " + r.getStatus() + " and locked.");
                if (en.version() == null || !en.version().equals(r.getVersion()))
                    throw new ObjectOptimisticLockingFailureException(ExamResult.class, r.getResultId());
            }
            r.setMarksObtained(en.absent() ? null : en.marksObtained());
            r.setAbsent(en.absent());
            r.setRemarks(en.remarks() == null || en.remarks().isBlank() ? null : en.remarks().trim());
            r.setStatus(ResultStatus.DRAFT);
            r.setReviewComment(null);
            resultRepo.save(r);
        }
        return build(exam, resultRepo.findByExamWithStudents(examId), roster);
    }

    /** Every student in the class needs marks or an absent flag before submitting. */
    @Transactional
    public ResultsResponse submit(Long examId, Authentication auth) throws AccessDeniedException {
        Exam exam = load(examId);
        assertAdminOrOwner(exam, auth);
        List<Student> roster = roster(exam);
        List<ExamResult> rows = resultRepo.findByExamWithStudents(examId);
        Map<Long, ExamResult> byStudent = rows.stream()
                .collect(Collectors.toMap(r -> r.getStudent().getStudentId(), r -> r));

        List<String> missing = roster.stream()
                .filter(s -> {
                    ExamResult r = byStudent.get(s.getStudentId());
                    return r == null || (r.getMarksObtained() == null && !r.isAbsent());
                })
                .map(s -> s.getFirstName() + " " + s.getLastName())
                .toList();
        if (!missing.isEmpty())
            throw new IllegalStateException(missing.size() + " student(s) have no marks or absent flag, e.g. "
                    + String.join(", ", missing.subList(0, Math.min(3, missing.size()))) + ".");

        int changed = 0;
        for (ExamResult r : rows) {
            if (r.getStatus() == ResultStatus.DRAFT || r.getStatus() == ResultStatus.REJECTED) {
                r.setStatus(ResultStatus.SUBMITTED);
                r.setReviewComment(null);
                changed++;
            }
        }
        if (changed == 0) throw new IllegalStateException("There is nothing to submit.");
        // TODO: notificationService.notifyAdmins("Results submitted", ...)
        return build(exam, rows, roster);
    }

    /** Admin only (enforced in the controller). Submitted rows are locked, so no version is needed. */
    @Transactional
    public ResultsResponse review(Long examId, ReviewResultsRequest req) {
        Exam exam = load(examId);
        List<ExamResult> rows = resultRepo.findByExamWithStudents(examId);
        List<ExamResult> submitted = rows.stream()
                .filter(r -> r.getStatus() == ResultStatus.SUBMITTED).toList();
        if (submitted.isEmpty()) throw new IllegalStateException("No submitted results to review.");
        if (!req.approve() && (req.comment() == null || req.comment().isBlank()))
            throw new IllegalStateException("Give a reason when rejecting results.");

        for (ExamResult r : submitted) {
            r.setStatus(req.approve() ? ResultStatus.APPROVED : ResultStatus.REJECTED);
            r.setReviewComment(req.comment() == null ? null : req.comment().trim());
        }
        // TODO: notify the teacher
        return build(exam, rows, roster(exam));
    }

    // ---------- helpers ----------

    private ResultsResponse build(Exam exam, List<ExamResult> rows, List<Student> roster) {
        Map<Long, ExamResult> byStudent = rows.stream()
                .collect(Collectors.toMap(r -> r.getStudent().getStudentId(), r -> r));
        List<ResultRow> out = roster.stream()
                .map(s -> ResultRow.of(s, byStudent.get(s.getStudentId()), exam.getTotalMarks()))
                .toList();

        Set<ResultStatus> statuses = rows.stream().map(ExamResult::getStatus).collect(Collectors.toSet());
        String status = rows.isEmpty() ? "NOT_STARTED"
                : (statuses.size() == 1 && rows.size() == roster.size())
                ? statuses.iterator().next().name() : "IN_PROGRESS";

        Assignment a = exam.getAssignment();
        return new ResultsResponse(exam.getExamId(), exam.getName(), a.getClasses().getName(),
                a.getSubject().getName(), exam.getTotalMarks(), status, out);
    }

    private List<Student> roster(Exam exam) {
        return studentRepo.findByClassWithUser(exam.getAssignment().getClasses().getClassesId())
                .stream()
                .sorted(Comparator.comparing((Student s) -> s.getLastName())
                        .thenComparing(s -> s.getFirstName()))
                .toList();
    }

    private Exam load(Long id) {
        return examRepo.findByIdWithDetails(id)
                .orElseThrow(() -> new IllegalArgumentException("Exam not found"));
    }

    private void assertAdminOrOwner(Exam e, Authentication auth) throws AccessDeniedException {
        boolean admin = auth.getAuthorities().stream().anyMatch(g -> g.getAuthority().equals("ROLE_ADMIN"));
        String owner = e.getAssignment().getTeacher().getUserReg().getEmail();
        if (!admin && !owner.equalsIgnoreCase(auth.getName()))
            throw new AccessDeniedException("You can only enter marks for your own exams.");
    }
}