package com.school.request;

import com.school.entity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkAttendanceRequest {
    private Long studentId;
    private AttendanceStatus status;
}

