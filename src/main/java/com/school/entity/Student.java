
package com.school.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(
        name = "students_tbl"
)
public class Student {
 @Id
 @SequenceGenerator(
         name = "student_gen",
         sequenceName = "student_gen",
         allocationSize = 1
 )
 @GeneratedValue(
         strategy = GenerationType.SEQUENCE,
         generator = "student_gen"
 )
 private Long studentId;
 private String name;
 private String email;
 @OneToOne(
         mappedBy = "student",
         cascade = CascadeType.ALL,
         orphanRemoval = true
 )
 private Enrollment enrollments;



}
