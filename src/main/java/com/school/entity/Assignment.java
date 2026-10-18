package com.school.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * "This teacher teaches this subject in this class."
 * A teacher can have many assignments (several subjects, several classes).
 * A class can have only ONE teacher per subject.
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "assignments",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_assignment_class_subject",
                columnNames = {"classes_id", "subject_id"}
        )
)
public class Assignment {
    @Id
    @SequenceGenerator(
            name = "assign_gen",
            sequenceName = "assign_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "assign_gen"
    )
    private Long assignmentId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", referencedColumnName = "teacherId")
    private Teacher teacher;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", referencedColumnName = "subjectId")
    private Subject subject;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "classes_id", referencedColumnName = "classesId")
    private SchoolClasses classes;

    @OneToMany(
            mappedBy = "assignment"
    )
    private List<Exam> exams = new ArrayList<>();

    // Identity-based equality. Lombok's @Data would include teacher, subject and classes in
    // equals/hashCode/toString, and those point back to their assignments: a StackOverflowError.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Assignment other)) return false;
        return assignmentId != null && assignmentId.equals(other.getAssignmentId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}