
package com.school.controller;

import com.school.request.MarkAttendanceRequest;
import com.school.response.AttendanceResponse;
import com.school.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * The core day-to-day flow: a teacher opens a Lesson, gets the class list,
 * and submits the whole register in one call. Admin reporting endpoints
 * (by student, by student+subject) sit alongside it.
 */
@RestController
//@RequestMapping("/api")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;


    @PostMapping("/lessons/{lessonId}/attendance")
    public ResponseEntity<List<AttendanceResponse>> markAttendance(
            @PathVariable Long lessonId,
            @RequestBody List<MarkAttendanceRequest> marks,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.markAttendanceForLesson(lessonId, marks, authentication));
    }

    @GetMapping("/lessons/{lessonId}/attendance")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceForLesson(
            @PathVariable Long lessonId, Authentication authentication) {
        return ResponseEntity.ok(attendanceService.getAttendanceForLesson(lessonId, authentication));
    }

    @GetMapping("/students/{studentId}/attendance")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceForStudent(
            @PathVariable Long studentId, Authentication authentication) {
        return ResponseEntity.ok(attendanceService.getAttendanceForStudent(studentId, authentication));
    }

    @GetMapping(value = "/students/{studentId}/attendance", params = "subjectId")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceForStudentInSubject(
            @PathVariable Long studentId,
            @RequestParam Long subjectId,
            Authentication authentication) {
        return ResponseEntity.ok(
                attendanceService.getAttendanceForStudentInSubject(studentId, subjectId, authentication));
    }
}