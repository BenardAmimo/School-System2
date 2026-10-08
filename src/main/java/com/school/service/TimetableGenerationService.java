package com.school.service;


import com.school.entity.Lesson;
import com.school.entity.TimetableSlot;
import com.school.repo.LessonRepository;
import com.school.repo.TimetableSlotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
public class TimetableGenerationService implements TimeTableGenerateServe{

    private final TimetableSlotRepository timetableSlotRepository;
    private final LessonRepository lessonRepository;

    public TimetableGenerationService(TimetableSlotRepository timetableSlotRepository,
                                      LessonRepository lessonRepository) {
        this.timetableSlotRepository = timetableSlotRepository;
        this.lessonRepository = lessonRepository;
    }

    /**
     * Call this once at the start of a term, e.g.
     * generateLessons(termStartDate, termEndDate).
     * Safe to re-run - existsByTimetableSlot_IdAndDate skips lessons
     * that already exist for that slot+date.
     */
    @Transactional
    @Override
    public int generateLessons(LocalDate startDate, LocalDate endDate) {
        List<TimetableSlot> allSlots = timetableSlotRepository.findAll();
        int created = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            DayOfWeek day = date.getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                continue;
            }

            for (TimetableSlot slot : allSlots) {
                if (slot.getDayOfWeek() != day) {
                    continue;
                }
                if (lessonRepository.existsByTimetableSlot_TimeTableSlotIdAndDate(slot.getTimeTableSlotId(), date)) {
                    continue;
                }

                Lesson lesson = new Lesson();
                lesson.setTimetableSlot(slot);
                lesson.setClasses(slot.getClasses());
                lesson.setTeacher(slot.getTeacher());
                lesson.setSubject(slot.getSubject());
                lesson.setDate(date);
                lesson.setStartTime(slot.getStartTime());
                lesson.setEndTime(slot.getEndTime());

                lessonRepository.save(lesson);
                created++;
            }
        }
        return created;
    }
}