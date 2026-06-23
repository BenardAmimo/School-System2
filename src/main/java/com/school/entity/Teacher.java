
package com.school.entity;
import com.school.security.entity.UserReg;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.engine.internal.Cascade;

import java.util.List;
import java.util.prefs.PreferencesFactory;

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
 private String teacherNo;

 @OneToMany(
         mappedBy = "teacher",
         cascade = CascadeType.ALL,
         orphanRemoval = true
 )
 private List<Assignment> assignments;
 @ManyToOne(
         cascade = CascadeType.ALL
 )
 @JoinColumn(
         name = "school_id",
         referencedColumnName = "schoolId"
 )
 private School school;
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


}
