package com.school.controller;


import com.school.request.GenerateLessonsRequest;
import com.school.service.TimetableGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin-facing: turn the standing TimetableSlot template into actual dated
 * Lesson rows. Call once at the start of a term with the term's date range.
 * Safe to re-run (existsByTimetableSlot_IdAndDate skips duplicates), so this
 * also doubles as a "re-sync" endpoint if slots changed mid-term.
 */
@RestController
@RequestMapping("/timetable")
@RequiredArgsConstructor
public class TimetableGenerationController {

    private final TimetableGenerationService timetableGenerationService;

    @PostMapping("/generate-lessons")
    public ResponseEntity<Map<String, Object>> generateLessons(@RequestBody GenerateLessonsRequest request) {
        int created = timetableGenerationService.generateLessons(
                request.getStartDate(),
                request.getEndDate()
        );
        return ResponseEntity.ok(Map.of(
                "lessonsCreated", created,
                "startDate", request.getStartDate(),
                "endDate", request.getEndDate()
        ));
    }
}
