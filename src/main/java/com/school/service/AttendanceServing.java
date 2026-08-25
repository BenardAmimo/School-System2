package com.school.service;

import com.school.request.AttendanceRequest;
import com.school.response.AttendanceResponse;

import java.util.List;

public interface AttendanceServing {
    AttendanceResponse markAttendance(AttendanceRequest request);

    List<AttendanceResponse> getAllAttendance();
}
