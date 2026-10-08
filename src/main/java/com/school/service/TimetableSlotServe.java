package com.school.service;

import com.school.entity.TimetableSlot;

import java.time.DayOfWeek;
import java.time.LocalTime;

public interface TimetableSlotServe {

    TimetableSlot createSlot(Long classesId, Long subjectId, Long teacherId,
                             DayOfWeek day, LocalTime start, LocalTime end);
}
