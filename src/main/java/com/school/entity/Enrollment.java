package com.school.entity;

import com.school.security.entity.UserReg;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "enrolls",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"assignment_id","student_id"})
        }
)
public class Enrollment {
    @Id
    @SequenceGenerator(
            name = "enroll_gen",
            sequenceName = "enroll_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "enroll_gen"
    )
    private Long enrollmentId;

    @ManyToOne
    @JoinColumn(
            name = "assignment_id",
            referencedColumnName = "assignmentId"
    )
    private Assignment assignment;
    @OneToOne
    @JoinColumn(
            name = "student_id",
            referencedColumnName = "studentId"
    )
    private Student student;


}
