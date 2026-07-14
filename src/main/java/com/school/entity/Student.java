
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
 private String regNo;
 @OneToOne(
         mappedBy = "student",
         cascade = CascadeType.ALL,
         orphanRemoval = true
 )
 private Enrollment enrollments;
 @ManyToOne(
         cascade = CascadeType.ALL
 )
 @OneToOne(
         cascade = CascadeType.ALL,
         fetch = FetchType.EAGER,
         orphanRemoval = true
 )
 @JoinColumn(
         name = "user_id",
         referencedColumnName = "userId"
 )
 private UserReg userReg;
 @OneToMany(
         mappedBy = "students"
 )
 private List<Funds> funds;

}
