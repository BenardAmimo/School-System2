package com.school.repo;

import com.school.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByLesson_LessonId(Long lessonId);
    List<Attendance> findByStudent_StudentId(Long studentId);
    List<Attendance> findByStudent_StudentIdAndLesson_Subject_SubjectId(Long studentId, Long subjectId);
    boolean existsByStudent_StudentIdAndLesson_LessonId(Long studentId, Long lessonId);
}