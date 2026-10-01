
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

 // Reference info: subjects this teacher is qualified/allowed to teach.
 // Used to validate TimetableSlot creation ("is this teacher allowed to teach this subject?").

 @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
 @JoinColumn(name = "user_id", referencedColumnName = "userId")
 private UserReg userReg;

 @ManyToMany
 @JoinTable(
         name = "teacher_subjects",
         joinColumns = @JoinColumn(name = "teacher_id"),
         inverseJoinColumns = @JoinColumn(name = "subject_id")
 )
 private List<Subject> subjects = new ArrayList<>();

 // Classes this teacher teaches into (many-to-many: a class has several
// subject teachers, a teacher can teach into several classes).
 @ManyToMany
 @JoinTable(
         name = "teacher_classes",
         joinColumns = @JoinColumn(name = "teacher_id"),
         inverseJoinColumns = @JoinColumn(name = "classes_id")
 )
 private List<SchoolClasses> classes = new ArrayList<>();

 @OneToMany(mappedBy = "teacher")
 private List<TimetableSlot> timetableSlots = new ArrayList<>();

 @OneToMany(mappedBy = "teacher")
 private List<Lesson> lessons = new ArrayList<>();

}


