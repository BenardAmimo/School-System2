package com.school.repo;

import com.school.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

    @Query("""
        select l from Lesson l
        join fetch l.classes c
        join fetch l.subject
        join fetch l.teacher t
        join fetch t.userReg
        where c.classesId = :classesId and l.date = :date
        order by l.startTime
    """)
    List<Lesson> findByClassAndDate(@Param("classesId") Long classesId,
                                    @Param("date") LocalDate date);

    @Query("""
        select l from Lesson l
        join fetch l.classes
        join fetch l.subject
        join fetch l.teacher t
        join fetch t.userReg
        where t.teacherId = :teacherId and l.date = :date
        order by l.startTime
    """)
    List<Lesson> findByTeacherAndDate(@Param("teacherId") Long teacherId,
                                      @Param("date") LocalDate date);

    boolean existsByTimetableSlot_TimeTableSlotIdAndDate(Long timeTableSlotId, LocalDate date);
}