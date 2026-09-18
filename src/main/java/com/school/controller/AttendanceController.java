package com.school.controller;

import com.school.request.AttendanceRequest;
import com.school.response.AttendanceResponse;
import com.school.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/attendance")
    public ResponseEntity <List<AttendanceResponse>> getAllAttendance(){
        List<AttendanceResponse> attendings = attendanceService.getAllAttendance();
        return ResponseEntity.ok(attendings);
    }

    @GetMapping("/id/{attendanceId}")
    public ResponseEntity <AttendanceResponse> getByAttendanceId(@PathVariable Long attendanceId){
        AttendanceResponse attendRespo = attendanceService.getAttendanceById(attendanceId);

        return ResponseEntity.ok(attendRespo);
    }

    @DeleteMapping("/id/{attendanceId}")
    public ResponseEntity<String> deleteAttendance(@PathVariable Long attendanceId){
         attendanceService.deleteAttendance(attendanceId);
        return ResponseEntity.ok("Deleted");

    }
}
