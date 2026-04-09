
package com.school.entity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
        name = "teachers_tbl"
)
public class Teacher {
 @Id
 @SequenceGenerator(
         name = "teacher_gen",
         sequenceName = "teacher_gen",
         allocationSize = 1
 )
 @GeneratedValue(
         strategy = GenerationType.SEQUENCE,
         generator = "teacher_gen"
 )
 private Long teacherId;
 private String name;
 private String email;

}
