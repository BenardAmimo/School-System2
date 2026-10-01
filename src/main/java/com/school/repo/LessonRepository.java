package com.school.repo;

import com.school.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    List<Lesson> findByClasses_ClassesIdAndDate(Long classesId, LocalDate date);
    List<Lesson> findByTeacher_TeacherIdAndDate(Long teacherId, LocalDate date);
    boolean existsByTimetableSlot_TimeTableSlotIdAndDate(Long timeTableSlotId, LocalDate date);
}
