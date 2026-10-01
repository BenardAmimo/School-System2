package com.school.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * One concrete, dated teaching event: this subject, taught by this teacher,
 * to this classroom, on this date/time. Either generated automatically from
 * a TimetableSlot (normal case) or created ad-hoc with timetableSlot = null
 * (e.g. a one-off substitution or makeup lesson).
 *
 * Attendance always anchors to a Lesson, never directly to Classroom/Teacher/
 * Subject - this is what guarantees every attendance row reflects a real,
 * valid teaching combination.
 */
@Entity
@Table(name = "lessons")
@Getter
@Setter
@NoArgsConstructor
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long LessonId;

    // Nullable: a Lesson can exist without a template (ad-hoc/manual lesson).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "timetable_slot_id")
    private TimetableSlot timetableSlot;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classes_id", nullable = false)
    private SchoolClasses classes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private Teacher teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(name = "lesson_date", nullable = false)
    private LocalDate date;

    private LocalTime startTime;
    private LocalTime endTime;

    @OneToMany(mappedBy = "lesson", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Attendance> attendanceRecords = new ArrayList<>();
}
