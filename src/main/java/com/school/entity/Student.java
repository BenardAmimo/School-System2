package com.school.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
