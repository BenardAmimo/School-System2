package com.school.service;

import com.school.request.AttendanceRequest;
import com.school.response.AttendanceResponse;

public interface AttendanceServing {
    AttendanceResponse markAttendance(AttendanceRequest request);
}
