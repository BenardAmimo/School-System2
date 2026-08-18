package com.school.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Attendance {
    @Id
    @SequenceGenerator(
            name = "attend_gen",
            sequenceName = "attend_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE
    )
    private Long attendanceId;
    private LocalDateTime attendingTime;
    private LocalDateTime checkoutTime;

    //Handle the mapping the issue with the Students and Subjects
    @OneToMany(
            mappedBy = "attendance"
    )
    private List<Student> student = new ArrayList<>();
    @OneToOne(
            mappedBy = "attendance"
    )
    private Subject subjects;

    @ManyToOne
    @JoinColumn(
            name = "classes_id",
            referencedColumnName = "classesId"
    )
    private SchoolClasses classes;
}
