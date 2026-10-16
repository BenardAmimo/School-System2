package com.school.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
public class Exam {
    @Id
    @SequenceGenerator(
            name = "exam_gen",
            sequenceName = "exam_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "exam_gen"
    )
    private Long examId;
    @Version
    private Long version;

    @Column(nullable = false)
    private String name;                  // e.g. "Form 2 Maths Mid-term"

    @Column(nullable = false)
    private LocalDate startDate;
    private LocalDate endDate;            // same as startDate for a one-day exam
    private LocalTime startTime;
    private LocalTime endTime;

    private int totalMarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamStatus status = ExamStatus.DRAFT;

    private String reviewComment;

    @Enumerated(EnumType.STRING)
    private ExamType examType;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    private Term term;
}
