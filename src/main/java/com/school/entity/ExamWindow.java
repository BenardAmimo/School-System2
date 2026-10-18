package com.school.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Getter
@Setter
public class ExamWindow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long windowId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "exam_period_id")
    private ExamPeriod examPeriod;

    @Column(nullable = false) private LocalDate date;
    @Column(nullable = false) private LocalTime startTime;
    @Column(nullable = false) private LocalTime endTime;
}
