package com.school.service;

import com.school.entity.TimetableSlot;
import com.school.request.CreateTimetableSlotRequest;
import com.school.response.TimetableSlotResponse;

import java.time.DayOfWeek;
import java.time.LocalTime;

public interface TimetableSlotServe {

    TimetableSlotResponse createSlot(CreateTimetableSlotRequest request);
}
