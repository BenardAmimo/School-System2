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
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "stud_gen"
    )
    private Long studentId;
    private String firstName;
    private String lastName;
    @ManyToOne()
    @JoinColumn(
            name = "parent_id",
            referencedColumnName = "parentId"
    )
    private Parent parent;

    @ManyToOne()
    @JoinColumn(
            name = "classes_id",
            referencedColumnName = "classesId"
    )
    private SchoolClasses classes;

    @OneToMany(
            mappedBy = "students"
    )
    private List<Funds> funds = new ArrayList<>();
}
