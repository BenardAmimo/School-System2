package com.school.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceRequest {
    private LocalDateTime attendingTime;
    private LocalDateTime checkoutTime;
    private String studentsFirstName;
    private String StudentsLastName;
    private String className;
    private String subject;
}
