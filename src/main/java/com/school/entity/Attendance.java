
package com.school.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * "For this Lesson, this Student was PRESENT/ABSENT/LATE/EXCUSED."
 * All the "which class, which teacher, which subject" context comes
 * transitively through lesson.getClasses() / .getTeacher() / .getSubject() -
 * Attendance itself only needs Student + Lesson.
 */
@Entity
@Table(name = "attendance", uniqueConstraints = {
        // one attendance mark per student per lesson - no double-marking
        @UniqueConstraint(name = "uk_student_lesson", columnNames = {"student_id", "lesson_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long attendanceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttendanceStatus status;
}