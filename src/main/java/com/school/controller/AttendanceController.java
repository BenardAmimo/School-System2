
package com.school.controller;

import com.school.request.MarkAttendanceRequest;
import com.school.response.AttendanceResponse;
import com.school.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // POST /api/lessons/{lessonId}/attendance
    // Body: [ { "studentId": 1, "status": "PRESENT" }, { "studentId": 2, "status": "ABSENT" } ]
    @PostMapping("/lessons/{lessonId}/attendance")
    public ResponseEntity<List<AttendanceResponse>> markAttendance(
            @PathVariable Long lessonId,
            @RequestBody List<MarkAttendanceRequest> marks) {
        List<AttendanceResponse> saved = attendanceService.markAttendanceForLesson(lessonId, marks);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // GET /api/lessons/{lessonId}/attendance  -> "today's Math register for Class 4B"
    @GetMapping("/lessons/{lessonId}/attendance")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceForLesson(@PathVariable Long lessonId) {
        return ResponseEntity.ok(attendanceService.getAttendanceForLesson(lessonId));
    }

    // GET /api/students/{studentId}/attendance  -> "Jane's attendance across everything"
    @GetMapping("/students/{studentId}/attendance")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceForStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(attendanceService.getAttendanceForStudent(studentId));
    }

    // GET /api/students/{studentId}/attendance?subjectId=4 -> "Jane's Science attendance only"
    @GetMapping(value = "/students/{studentId}/attendance", params = "subjectId")
    public ResponseEntity<List<AttendanceResponse>> getAttendanceForStudentInSubject(
            @PathVariable Long studentId,
            @RequestParam Long subjectId) {
        return ResponseEntity.ok(attendanceService.getAttendanceForStudentInSubject(studentId, subjectId));
    }
}