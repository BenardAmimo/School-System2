package com.school.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"term_id", "exam_type"}))
public class ExamPeriod {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long examPeriodId;

    @Version private Long version;

    @Column(nullable = false) private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "exam_type", nullable = false)
    private ExamType examType;

    @Column(nullable = false) private LocalDate startDate;
    @Column(nullable = false) private LocalDate endDate;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id")
    private Term term;

    @OneToMany(mappedBy = "examPeriod", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExamWindow> windows = new ArrayList<>();
}
