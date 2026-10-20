package com.school.service;

import com.school.entity.*;
import com.school.repo.ExamResultRepository;
import com.school.repo.StudentRepository;
import com.school.repo.TermRepository;
import com.school.response.BestStudent;
import com.school.response.ClassRank;
import com.school.response.ReportCard;
import com.school.response.SubjectLine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ReportService {

    private final ExamResultRepository resultRepo;
    private final StudentRepository studentRepo;
    private final TermRepository termRepo;


    public ReportService(ExamResultRepository resultRepo, StudentRepository studentRepo, TermRepository termRepo) {
        this.resultRepo = resultRepo;
        this.studentRepo = studentRepo;
        this.termRepo = termRepo;
    }

    @Value("${app.grading.mid-weight:0.3}") private double midWeight;
    @Value("${app.grading.end-weight:0.7}") private double endWeight;


    private static class Pair { Double mid, end; }

    private static class Data {
        final Map<Long, Map<Long, Pair>> scores = new HashMap<>();   // student -> subject -> pair
        final Map<Long, String> subjects = new HashMap<>();
        final Map<Long, String> students = new HashMap<>();
    }

    @Transactional(readOnly = true)
    public ReportCard reportCard(Long studentId, Long termId) {
        Student st = studentRepo.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        SchoolClasses cls = st.getClasses();
        Term term = termRepo.findById(termId)
                .orElseThrow(() -> new IllegalArgumentException("Term not found"));

        Data d = load(cls.getClassesId(), termId);
        Map<Long, Double> avg = averages(d);
        Map<Long, Pair> mine = d.scores.getOrDefault(studentId, Map.of());

        List<SubjectLine> lines = mine.entrySet().stream()
                .map(e -> {
                    Pair p = e.getValue();
                    Double fin = combine(p);
                    return new SubjectLine(d.subjects.get(e.getKey()), p.mid, p.end, fin,
                            fin == null ? null : Grading.grade(fin));
                })
                .sorted(Comparator.comparing(SubjectLine::subjectName))
                .toList();

        double total = lines.stream().filter(l -> l.finalPercent() != null)
                .mapToDouble(SubjectLine::finalPercent).sum();
        Double average = avg.get(studentId);

        return new ReportCard(
                studentId,
                st.getFirstName() + " " + st.getLastName(),
                cls.getName(), term.getName(), lines,
                Grading.round2(total), average,
                average == null ? null : Grading.grade(average),
                average == null ? null : position(avg, average),
                studentRepo.countByClasses_ClassesId(cls.getClassesId()));
    }

    @Transactional(readOnly = true)
    public List<ClassRank> classRanking(Long classId, Long termId) {
        Data d = load(classId, termId);
        Map<Long, Double> avg = averages(d);
        return avg.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(e -> new ClassRank(position(avg, e.getValue()), e.getKey(),
                        d.students.get(e.getKey()), e.getValue(), Grading.grade(e.getValue())))
                .toList();
    }

    /** type = null means the weighted mid + end score. Ties return every tied student. */
    @Transactional(readOnly = true)
    public List<BestStudent> bestStudents(Long classId, Long termId, ExamType type) {
        Data d = load(classId, termId);
        List<BestStudent> out = new ArrayList<>();

        d.subjects.forEach((subjectId, subjectName) -> {
            Map<Long, Double> scores = new HashMap<>();
            d.scores.forEach((studentId, bySubject) -> {
                Pair p = bySubject.get(subjectId);
                if (p == null) return;
                Double v = type == ExamType.MID_TERM ? p.mid
                        : type == ExamType.END_TERM ? p.end : combine(p);
                if (v != null) scores.put(studentId, v);
            });
            if (scores.isEmpty()) return;

            double max = Collections.max(scores.values());
            List<String> names = scores.entrySet().stream()
                    .filter(e -> e.getValue() == max)
                    .map(e -> d.students.get(e.getKey()))
                    .sorted().toList();
            out.add(new BestStudent(subjectName, max, names));
        });
        out.sort(Comparator.comparing(BestStudent::subjectName));
        return out;
    }

    // ---------- helpers ----------

    private Data load(Long classId, Long termId) {
        Data d = new Data();
        for (ExamResult r : resultRepo.findByClassAndTermAndStatus(classId, termId, ResultStatus.APPROVED)) {
            if (r.isAbsent() || r.getMarksObtained() == null) continue;   // absent never counts as zero
            Exam e = r.getExam();
            Subject sub = e.getAssignment().getSubject();
            Student st = r.getStudent();
            double pct = Grading.round2(r.getMarksObtained() * 100.0 / e.getTotalMarks());

            Pair p = d.scores.computeIfAbsent(st.getStudentId(), k -> new HashMap<>())
                    .computeIfAbsent(sub.getSubjectId(), k -> new Pair());
            if (e.getExamPeriod().getExamType() == ExamType.MID_TERM) p.mid = pct; else p.end = pct;

            d.subjects.put(sub.getSubjectId(), sub.getName());
            d.students.put(st.getStudentId(),
                    st.getFirstName() + " " + st.getLastName());
        }
        return d;
    }

    /** Both exams: weighted. Only one sat: that one. None: null. */
    private Double combine(Pair p) {
        if (p.mid != null && p.end != null)
            return Grading.round2(p.mid * midWeight + p.end * endWeight);
        return p.mid != null ? p.mid : p.end;
    }

    private Map<Long, Double> averages(Data d) {
        Map<Long, Double> avg = new HashMap<>();
        d.scores.forEach((studentId, bySubject) -> {
            List<Double> finals = bySubject.values().stream()
                    .map(this::combine).filter(Objects::nonNull).toList();
            if (!finals.isEmpty())
                avg.put(studentId, Grading.round2(finals.stream().mapToDouble(Double::doubleValue).average().orElse(0)));
        });
        return avg;
    }

    /** 1, 2, 2, 4 style: equal averages share a position. */
    private int position(Map<Long, Double> avg, double mine) {
        return 1 + (int) avg.values().stream().filter(v -> v > mine).count();
    }
}