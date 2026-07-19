package com.school.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "Students_tbl"
)
public class Student {
    @Id
    @SequenceGenerator(
            name = "stud_gen",
            sequenceName = "stud_gen",
            allocationSize = 1
    )
    private Long studentId;
    private String firstName;
    private String lastName;
    @OneToOne(
            mappedBy = "student"
    )
    private Parent parent;

    @OneToMany(
            mappedBy = "student"
    )
    private List<Subject> subject = new ArrayList<>();

    @ManyToOne()
    @JoinColumn(
            name = "classes_id",
            referencedColumnName = "classesId"
    )
    private SchoolClasses classes;
}
