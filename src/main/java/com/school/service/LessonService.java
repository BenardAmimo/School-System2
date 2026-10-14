package com.school.service;

import com.school.entity.*;
import com.school.repo.LessonRepository;
import com.school.repo.SchoolClassesRepository;
import com.school.repo.SubjectRepository;
import com.school.repo.TeacherRepo;
import com.school.response.LessonResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class LessonService implements LessonServe {

    private final LessonRepository lessonRepository;
    private final SchoolClassesRepository schoolClassesRepository;
    private final TeacherRepo teacherRepository;
    private final SubjectRepository subjectRepository;

    public LessonService(LessonRepository lessonRepository,
                         SchoolClassesRepository schoolClassesRepository,
                         TeacherRepo teacherRepository,
                         SubjectRepository subjectRepository) {
        this.lessonRepository = lessonRepository;
        this.schoolClassesRepository = schoolClassesRepository;
        this.teacherRepository = teacherRepository;
        this.subjectRepository = subjectRepository;
    }

    /**
     * Normal flow: lessons already exist because generateLessons() ran for the term.
     */
    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsForClassOnDate(Long classesId, LocalDate date) {
        return lessonRepository.findByClassAndDate(classesId, date)
                .stream()
                .map(LessonResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getLessonsForTeacherOnDate(Long teacherId, LocalDate date) {
        return lessonRepository.findByTeacherAndDate(teacherId, date)
                .stream()
                .map(LessonResponse::from)
                .toList();
    }

    /**
     * Ad-hoc / one-off lesson, e.g. a substitution or makeup class that isn't
     * part of the standing weekly timetable. timetableSlot is left null.
     */
    @Transactional
    public LessonResponse createAdHocLesson(Long classesId, Long teacherId, Long subjectId,
                                            LocalDate date, LocalTime start, LocalTime end) {

        SchoolClasses classes = schoolClassesRepository.findById(classesId)
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));
        Teacher teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("Subject not found"));

        Lesson lesson = new Lesson();
        lesson.setClasses(classes);
        lesson.setTeacher(teacher);
        lesson.setSubject(subject);
        lesson.setDate(date);
        lesson.setStartTime(start);
        lesson.setEndTime(end);
        // timetableSlot stays null - this lesson has no recurring template behind it
        Lesson lesson1 = lessonRepository.save(lesson);

        return LessonResponse.from(lesson1);
    }
}