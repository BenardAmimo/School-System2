package com.school.controller;

import com.school.entity.TimetableSlot;
import com.school.request.CreateTimetableSlotRequest;
import com.school.response.TimetableSlotResponse;
import com.school.service.TimetableSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Admin-facing: build the standing weekly timetable.
 * POST here once per slot when setting up (or editing) a term's timetable.
 */
@RestController
@RequestMapping("/timetable-slots")
@RequiredArgsConstructor
public class TimetableSlotController {

    private final TimetableSlotService timetableSlotService;

    // Was missing: this is what the Timetable page calls to list slots.
    @GetMapping
    public ResponseEntity<List<TimetableSlotResponse>> getAllSlots() {
        return ResponseEntity.ok(timetableSlotService.getAllSlots());
    }

    @PostMapping
    public ResponseEntity<TimetableSlotResponse> createSlot(@RequestBody CreateTimetableSlotRequest request) {
        TimetableSlot slot = timetableSlotService.createSlot(
                request.getClassesId(),
                request.getSubjectId(),
                request.getTeacherId(),
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime());
        return ResponseEntity.status(HttpStatus.CREATED).body(TimetableSlotResponse.from(slot));
    }

    // Was missing: api.js deleteTimetableSlot calls this.
    @DeleteMapping("/{timeTableSlotId}")
    public ResponseEntity<Void> deleteSlot(@PathVariable Long slotId) {
        timetableSlotService.deleteSlot(slotId);
        return ResponseEntity.noContent().build();
    }
}
