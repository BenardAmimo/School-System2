package com.school.request;

import com.school.entity.AttendanceStatus;

public record MarkAttendanceRequest(
        Long studentId,
        AttendanceStatus status
) {

}

