
package com.school.entity;
import com.school.security.entity.UserReg;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
         mappedBy = "parent"
 )
 private List<Student> student = new ArrayList<>();

}
