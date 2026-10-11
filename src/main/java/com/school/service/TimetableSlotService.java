
package com.school.service;

import com.school.entity.*;
import com.school.repo.SchoolClassesRepository;
import com.school.repo.SubjectRepository;
import com.school.repo.TeacherRepo;
import com.school.repo.TimetableSlotRepository;
import com.school.request.CreateTimetableSlotRequest;
import com.school.response.TimetableSlotResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Service
public class TimetableSlotService implements TimetableSlotServe{

    private final TimetableSlotRepository timetableSlotRepository;
    private final SchoolClassesRepository schoolClassesRepository;
    private final TeacherRepo teacherRepository;
    private final SubjectRepository subjectRepository;

    public TimetableSlotService(TimetableSlotRepository timetableSlotRepository,
                                SchoolClassesRepository schoolClassesRepository,
                                TeacherRepo teacherRepository,
                                SubjectRepository subjectRepository) {
        this.timetableSlotRepository = timetableSlotRepository;
        this.schoolClassesRepository = schoolClassesRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
    }

    @Transactional
    @Override
    public TimetableSlotResponse createSlot(CreateTimetableSlotRequest request) {

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        SchoolClasses classes = schoolClassesRepository.findById(request.getClassesId())
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        Subject subject = subjectRepository.findById(request.getSubjectId())
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        // Optional but recommended: only allow teachers to be slotted for
        // subjects they're actually assigned to teach (Teacher<->Subject many-to-many).
       if (!teacher.getSubjects().contains(subject)) {
         throw new IllegalStateException(
        teacher.getTeacherNo() + " is not assigned to teach " + subject.getName());//name
       }

        // DB unique constraints catch exact-match clashes; this catches
        // OVERLAPPING ranges too, e.g. 8:00-8:45 clashing with 8:30-9:15.
        assertNoClassOverlap(request.getClassesId(), request.getDayOfWeek(), request.getStartTime(), request.getEndTime());
        assertNoTeacherOverlap(request.getTeacherId(), request.getDayOfWeek(), request.getStartTime(), request.getEndTime());

        TimetableSlot slot = new TimetableSlot();
        slot.setClasses(classes);
        slot.setTeacher(teacher);
        slot.setSubject(subject);
        slot.setDayOfWeek(request.getDayOfWeek());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());

         TimetableSlot savedWell = timetableSlotRepository.save(slot);

         return TimetableSlotResponse.from(savedWell);

    }

    private void assertNoClassOverlap(Long classesId, DayOfWeek day, LocalTime start, LocalTime end) {
        List<TimetableSlot> existing = timetableSlotRepository.findByClasses_ClassesIdAndDayOfWeek(classesId, day);
        for (TimetableSlot slot : existing) {
            if (overlaps(start, end, slot.getStartTime(), slot.getEndTime())) {
                throw new IllegalStateException(
                        "This class already has " + slot.getSubject().getName() +
                                " scheduled at that time on " + day);
            }
        }
    }

    private void assertNoTeacherOverlap(Long teacherId, DayOfWeek day, LocalTime start, LocalTime end) {
        List<TimetableSlot> existing = timetableSlotRepository.findByTeacher_TeacherIdAndDayOfWeek(teacherId, day);
        for (TimetableSlot slot : existing) {
            if (overlaps(start, end, slot.getStartTime(), slot.getEndTime())) {
                throw new IllegalStateException(
                        "This teacher is already teaching " + slot.getClasses().getName() +
                                " at that time on " + day);
            }
        }
    }

    private boolean overlaps(LocalTime aStart, LocalTime aEnd, LocalTime bStart, LocalTime bEnd) {
        return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
    }

    @Transactional(readOnly = true)
    public List<TimetableSlotResponse> getAllSlots() {
        return timetableSlotRepository
                .findAll()
                .stream()
                .map(TimetableSlotResponse::from)
                .toList();
    }

    @Transactional
    public void deleteSlot(Long slotId) {
        if (!timetableSlotRepository.existsById(slotId)) {
            throw new IllegalArgumentException("Timetable slot not found");
        }
        timetableSlotRepository.deleteById(slotId);
    }

}
