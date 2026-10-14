package com.school.service;

import com.school.request.CreateTimetableSlotRequest;
import com.school.response.TimetableSlotResponse;


public interface TimetableSlotServe {

    TimetableSlotResponse createSlot(CreateTimetableSlotRequest request);
    void deleteSlot(Long timeTableSlotId);
}
