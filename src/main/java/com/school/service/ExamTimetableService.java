package com.school.service;

import com.school.entity.Assignment;
import com.school.entity.Exam;
import com.school.entity.ExamPeriod;
import com.school.entity.ExamWindow;
import com.school.repo.AssignmentRepository;
import com.school.repo.ExamPeriodRepository;
import com.school.repo.ExamRepository;
import com.school.request.GenerateRequest;
import com.school.response.ExamResponse;
import com.school.response.GenerateResult;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamTimetableService {

    private final ExamPeriodRepository periodRepo;
    private final AssignmentRepository assignmentRepo;
    private final ExamRepository examRepo;

    @Transactional
    public GenerateResult generate(Long periodId, GenerateRequest req) {
        ExamPeriod period = periodRepo.findById(periodId)
                .orElseThrow(() -> new IllegalArgumentException("Exam period not found"));

        List<ExamWindow> windows = period.getWindows().stream()
                .sorted(Comparator.comparing(ExamWindow::getDate).thenComparing(ExamWindow::getStartTime))
                .toList();
        if (windows.isEmpty())
            throw new IllegalStateException("Add at least one exam window before generating.");

        // what each class already has booked (so generation can be re-run safely)
        List<Exam> existing = examRepo.findByPeriodBasic(periodId);
        Set<Long> alreadyDone = existing.stream()
                .map(e -> e.getAssignment().getAssignmentId()).collect(Collectors.toSet());
        Map<String, List<LocalTime[]>> busy = new HashMap<>();
        for (Exam e : existing) {
            busy.computeIfAbsent(key(e.getAssignment().getClasses().getClassesId(), e.getExamDate()),
                    k -> new ArrayList<>()).add(new LocalTime[]{e.getStartTime(), e.getEndTime()});
        }

        Map<Long, List<Assignment>> byClass = assignmentRepo.findAllWithDetails().stream()
                .filter(a -> !alreadyDone.contains(a.getAssignmentId()))
                .collect(Collectors.groupingBy(a -> a.getClasses().getClassesId()));

        List<Exam> created = new ArrayList<>();
        List<String> unplaced = new ArrayList<>();

        for (List<Assignment> papers : byClass.values()) {
            papers.sort(Comparator.comparingInt((Assignment a) -> minutes(a, req)).reversed());
            for (Assignment a : papers) {
                Exam placed = place(period, a, windows, busy, req);
                if (placed == null) unplaced.add(a.getClasses().getName() + " · " + a.getSubject().getName());
                else created.add(placed);
            }
        }
        examRepo.saveAll(created);
        return new GenerateResult(created.stream().map(ExamResponse::from).toList(), unplaced);
    }

    private Exam place(ExamPeriod period, Assignment a, List<ExamWindow> windows,
                       Map<String, List<LocalTime[]>> busy, GenerateRequest req) {
        int mins = minutes(a, req);
        Long classId = a.getClasses().getClassesId();

        for (ExamWindow w : windows) {
            List<LocalTime[]> day = busy.computeIfAbsent(key(classId, w.getDate()), k -> new ArrayList<>());
            if (day.size() >= req.maxPapersPerDay()) continue;

            LocalTime start = w.getStartTime();
            while (true) {
                LocalTime end = start.plusMinutes(mins);
                if (end.isBefore(start) || end.isAfter(w.getEndTime())) break;   // doesn't fit / wrapped midnight

                final LocalTime s = start, e = end;
                LocalTime[] clash = day.stream()
                        .filter(b -> b[0].isBefore(e) && b[1].isAfter(s))
                        .max(Comparator.comparing(b -> b[1])).orElse(null);

                if (clash == null) {
                    day.add(new LocalTime[]{s, e});
                    Exam exam = new Exam();
                    exam.setAssignment(a);
                    exam.setExamPeriod(period);
                    exam.setName(ExamService.defaultName(a, period));
                    exam.setExamDate(w.getDate());
                    exam.setStartTime(s);
                    exam.setEndTime(e);
                    exam.setTotalMarks(req.totalMarks());
                    return exam;
                }
                LocalTime next = clash[1].plusMinutes(req.breakMinutes());
                if (!next.isAfter(start)) break;
                start = next;
            }
        }
        return null;
    }

    private int minutes(Assignment a, GenerateRequest req) {
        Integer d = a.getSubject().getExamDurationMinutes();
        return d != null ? d : req.defaultDurationMinutes();
    }

    private String key(Long classId, LocalDate date) { return classId + "|" + date; }
}