package com.school.controller;

import com.school.request.AttendanceRequest;
import com.school.response.AttendanceResponse;
import com.school.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping("/attend")
    public ResponseEntity<AttendanceResponse> markAttendance(@RequestBody AttendanceRequest request){

        AttendanceResponse respo = attendanceService.markAttendance(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respo);

    }
}
