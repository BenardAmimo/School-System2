package com.school.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"exam_id", "student_id"}))
public class ExamResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultId;

    @Version
    private Long version;

    private Double marksObtained;                 // null = not entered yet
    @Column(nullable = false) private boolean absent = false;
    private String remarks;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ResultStatus status = ResultStatus.DRAFT;
    private String reviewComment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "exam_id")
    private Exam exam;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "student_id")
    private Student student;
}
