package com.school.response;


import com.school.entity.AttendanceStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Setter
@Getter
public class AttendanceResponse{
    private Long attendanceId;
     private Long studentId;
    private String studentName;
    private Long lessonId;
    private String subjectName;
    private String className;
    private LocalDate lessonDate;
    private AttendanceStatus status;


}
