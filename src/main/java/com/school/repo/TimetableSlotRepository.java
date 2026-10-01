package com.school.repo;

import com.school.entity.TimetableSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;

@Repository
public interface TimetableSlotRepository extends JpaRepository<TimetableSlot, Long> {

    List<TimetableSlot> findByDayOfWeek(DayOfWeek dayOfWeek);

    // used by validation to check clashes before saving a new slot
    List<TimetableSlot> findByClasses_ClassesIdAndDayOfWeek(Long classesId, DayOfWeek dayOfWeek);
    List<TimetableSlot> findByTeacher_TeacherIdAndDayOfWeek(Long teacherId, DayOfWeek dayOfWeek);
}
