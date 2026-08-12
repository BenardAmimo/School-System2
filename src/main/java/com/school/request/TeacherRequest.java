
package com.school.request;
import com.school.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherRequest {
 private String teacherNo;
 private String phoneNumber;
 private Gender gender;
 private Long age;
 private Long userId;
 private Long classesId;
}
