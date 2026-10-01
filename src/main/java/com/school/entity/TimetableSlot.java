package com.school.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalTime;

/**
 * The recurring weekly pattern: "every Monday 8:00-8:40, Class 4B has
 * Mathematics with Mr. Otieno". This does NOT represent a specific date -
 * that's what Lesson is for. TimetableGenerationService reads these slots
 * to generate actual Lesson rows for a date range (e.g. a whole term).
 */
@Entity
@Table(name = "timetable_slots", uniqueConstraints = {
        // a class can't have two subjects at the same day + start time
        @UniqueConstraint(name = "uk_class_day_time", columnNames = {"classes_id", "day_of_week", "start_time"}),
        // a teacher can't be in two places at the same day + start time
        @UniqueConstraint(name = "uk_teacher_day_time", columnNames = {"teacher_id", "day_of_week", "start_time"})
})
@Getter
@Setter
@NoArgsConstructor
public class TimetableSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long timeTableSlotId;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "classes_id", nullable = false)
    private SchoolClasses classes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek; // MONDAY..FRIDAY

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
}
