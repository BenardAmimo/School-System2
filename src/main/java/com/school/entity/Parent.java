
package com.school.entity;
import com.school.security.entity.UserReg;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(
        name = "parent_tbl"
)
public class Parent {
 @Id
 @SequenceGenerator(
         name = "parent_gen",
         sequenceName = "student_gen",
         allocationSize = 1
 )
 @GeneratedValue(
         strategy = GenerationType.SEQUENCE,
         generator = "parent_gen"
 )
 private Long parentId;

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

 @OneToOne(
 )@JoinColumn(
         name = "student_id",
         referencedColumnName = "studentId"
 )
 private Student student;

}
