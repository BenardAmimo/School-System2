
package com.school.entity;
import com.school.security.entity.UserReg;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
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
 private String teacherNo;
 private String phoneNumber;
 @Enumerated(EnumType.STRING)
 private Gender gender;
 private Long age;

 @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
 @JoinColumn(name = "user_id", referencedColumnName = "userId")
 private UserReg userReg;

 @OneToMany(mappedBy = "teacher")
 private List<TimetableSlot> timetableSlots = new ArrayList<>();

 @OneToMany(mappedBy = "teacher")
 private List<Lesson> lessons = new ArrayList<>();

}


