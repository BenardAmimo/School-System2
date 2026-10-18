package com.school.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity @Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"assignment_id", "exam_period_id"}))
public class Exam {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examId;

    @Version private Long version;

    @Column(nullable = false) private String name;
    @Column(nullable = false) private LocalDate examDate;
    @Column(nullable = false) private LocalTime startTime;
    @Column(nullable = false) private LocalTime endTime;
    @Column(nullable = false) private int totalMarks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExamStatus status = ExamStatus.DRAFT;

    private String reviewComment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "assignment_id")
    private Assignment assignment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_period_id")
    private ExamPeriod examPeriod;
}