package com.school.entity;

import com.school.security.entity.UserReg;
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
public class School {
    @Id
    @SequenceGenerator(
            sequenceName = "school_gen",
            name = "school_gen",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "school_gen"
    )
private Long schoolId;
private String name;
private String motto;
private String vision;
@OneToMany(
        mappedBy = "school"
)
private List<Teacher> teachers;

@OneToMany(
        mappedBy = "school"
)
private List<Student> students;
@OneToMany(
        mappedBy = "school"
)
private List<Course> courses;

}


