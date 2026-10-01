package com.school.response;

import com.school.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceResponse {
    private Long attendanceId;

    private Long studentId;
    private String studentName;

    private Long lessonId;
    private String subjectName;
    private String className;
    private LocalDate lessonDate;

    private AttendanceStatus status;
}