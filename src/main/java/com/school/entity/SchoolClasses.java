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
            , allocationSize = 1
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
            mappedBy = "schoolClasses",
            orphanRemoval = true
    )
    private List<Subject> subjects = new ArrayList<>();

    @OneToMany(mappedBy = "classes",
    orphanRemoval = true)
    private List<Teacher> teachers = new ArrayList<>();
    @OneToMany(
            mappedBy = "classes",
            orphanRemoval = true
    )
    private List<Attendance> attendance;
}