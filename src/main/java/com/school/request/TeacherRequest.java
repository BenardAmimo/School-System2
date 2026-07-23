
package com.school.request;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeacherRequest {
 private String teacherNo;
 private String phoneNumber;
 private Long userId;
 private Long classesId;
}
