package com.school.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "teacher_subject_assign",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"teacher_id","subject_id"}

        )}
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

    @ManyToOne
    @JoinColumn(name = "teacher_id",
            referencedColumnName = "teacherId" )
    private Teacher teacher;

    @ManyToOne
    @JoinColumn(
            name = "subject_id",
            referencedColumnName = "subjectId"
    )
    private Subject subject;


}