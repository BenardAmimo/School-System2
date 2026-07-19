
package com.school.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "subject_tbl"
)
public class Subject {
 @Id
 @SequenceGenerator(
         name = "subject_gen",
         sequenceName = "subject_gen",
         allocationSize = 1
 )
 @GeneratedValue(
         strategy = GenerationType.SEQUENCE,
 generator = "subject_gen"
 )
 private Long subjectId;
 private String name;
 private String description;
 @OneToMany(
         mappedBy = "subject",
         cascade = CascadeType.ALL,
         orphanRemoval = true
 )
 private List<Assignment> assignment;
 @ManyToOne()
 @JoinColumn(
         name = "student_id",
         referencedColumnName = "studentId"
 )
 private Student student;
 @ManyToOne()
 @JoinColumn(
        name = "classes_id",
         referencedColumnName = "classesId"
 )
 private SchoolClasses schoolClasses;


}
