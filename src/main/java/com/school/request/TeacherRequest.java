
package com.school.request;
import com.school.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherRequest {
 private String teacherNo;
 private String phoneNumber;
 private Gender gender;
 private Long age;
 private Long userId;
 //private Long classesId;
 private List<Long> classesId;
 private List<Long> subjectId;
}
