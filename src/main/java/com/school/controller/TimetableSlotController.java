package com.school.controller;

import com.school.entity.TimetableSlot;
import com.school.request.CreateTimetableSlotRequest;
import com.school.service.TimetableSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Admin-facing: build the standing weekly timetable.
 * POST here once per slot when setting up (or editing) a term's timetable.
 */
@RestController
@RequestMapping("/timetable-slots")
@RequiredArgsConstructor
public class TimetableSlotController {

    private final TimetableSlotService timetableSlotService;

    @PostMapping
    public ResponseEntity<TimetableSlot> createSlot(@RequestBody CreateTimetableSlotRequest request) {
        TimetableSlot slot = timetableSlotService.createSlot(
                request.getClassesId(),
                request.getSubjectId(),
                request.getTeacherId(),
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(slot);
    }

    // createSlot() throws IllegalArgumentException (not found) and
    // IllegalStateException (clash/not-qualified) - handle both in your
    // global @ControllerAdvice and map to 404 / 409 respectively.
}
