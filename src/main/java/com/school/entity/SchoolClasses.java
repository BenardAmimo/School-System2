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

    // The homeroom / class teacher responsible for this class overall.
   // @ManyToOne(fetch = FetchType.LAZY)
   // @JoinColumn(name = "class_teacher_id")
  //  private Teacher teacher;

    @ManyToMany(mappedBy = "classes")
    private List<Teacher> teachers = new ArrayList<>();

    @OneToMany(mappedBy = "classes")
    private List<Student> students = new ArrayList<>();

    // Reference info: which subjects this class studies in its curriculum.
    @ManyToMany
    @JoinTable(
            name = "class_subjects",
            joinColumns = @JoinColumn(name = "class_id"),
            inverseJoinColumns = @JoinColumn(name = "subject_id")
    )
    private List<Subject> subjects = new ArrayList<>();

    @OneToMany(mappedBy = "classes")
    private List<TimetableSlot> timetableSlots = new ArrayList<>();

    @OneToMany(mappedBy = "classes")
    private List<Lesson> lessons = new ArrayList<>();
}
