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
public class SchoolClasses {
    @Id
    @SequenceGenerator(
            name = "class_gen",
            sequenceName = "class_gen"
            ,allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "class_gen"
    )
    private Long classesId;
    private String location;
    private String name;
    private String year;
    @OneToMany(
            mappedBy = "classes"
    )
    private List<Student> student = new ArrayList<>();

    @OneToMany(
            mappedBy = "schoolClasses"
    )
    private List<Subject> subjects = new ArrayList<>();

}
