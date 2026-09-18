
package com.school.entity;
import com.school.security.entity.UserReg;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;


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
         sequenceName = "parent_gen",
         allocationSize = 1
 )
 @GeneratedValue(
         strategy = GenerationType.SEQUENCE,
         generator = "parent_gen"
 )
 private Long parentId;
 private String phoneNumber;
 private Long age;
 @Enumerated(EnumType.STRING)
 private Gender gender;

 @OneToOne(
         orphanRemoval = true
 )
 @JoinColumn(
         name = "user_id",
         referencedColumnName = "userId"
 )
 private UserReg userReg;

 @OneToMany(
         mappedBy = "parent",
         orphanRemoval = true
 )
 private List<Student> student = new ArrayList<>();

}
