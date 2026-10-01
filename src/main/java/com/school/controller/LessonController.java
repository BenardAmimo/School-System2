package com.school.controller;

import com.school.entity.Lesson;
import com.school.request.CreateAdHocLessonRequest;
import com.school.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Teacher-facing: "what am I teaching today", plus admin ad-hoc/substitution
 * lesson creation. Attendance is always marked against a Lesson id returned
 * from here (or from generate-lessons).
 */
@RestController
@RequestMapping("/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    // GET /api/lessons?classesId=3&date=2026-09-29
    @GetMapping(params = "classesId")
    public ResponseEntity<List<Lesson>> getLessonsForClass(
            @RequestParam Long classesId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(lessonService.getLessonsForClassOnDate(classesId, date));
    }

    // GET /api/lessons?teacherId=7&date=2026-09-29
    @GetMapping(params = "teacherId")
    public ResponseEntity<List<Lesson>> getLessonsForTeacher(
            @RequestParam Long teacherId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(lessonService.getLessonsForTeacherOnDate(teacherId, date));
    }

    // For a substitution / makeup lesson that isn't on the standing timetable.
    @PostMapping("/ad-hoc")
    public ResponseEntity<Lesson> createAdHocLesson(@RequestBody CreateAdHocLessonRequest request) {
        Lesson lesson = lessonService.createAdHocLesson(
                request.getClassesId(),
                request.getTeacherId(),
                request.getSubjectId(),
                request.getDate(),
                request.getStartTime(),
                request.getEndTime()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(lesson);
    }
}
