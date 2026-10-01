
package com.school.service;


import com.school.entity.*;
import com.school.repo.AttendanceRepository;
import com.school.repo.LessonRepository;
import com.school.repo.StudentRepository;
import com.school.request.MarkAttendanceRequest;
import com.school.response.AttendanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;

    /**
     * The main teacher-facing action: "for this lesson, here's the status
     * of every student in the class." One call, one lesson, whole register.
     */
    @Transactional
    public List<AttendanceResponse> markAttendanceForLesson(Long lessonId, List<MarkAttendanceRequest> marks) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        List<Attendance> saved = new ArrayList<>();

        for (MarkAttendanceRequest mark : marks) {
            if (attendanceRepository.existsByStudent_StudentIdAndLesson_LessonId(mark.getStudentId(), lessonId)) {
                // already marked - skip rather than throw, so re-submitting a
                // partially-saved register doesn't blow up the whole request
                continue;
            }

            Student student = studentRepository.findById(mark.getStudentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Student not found: " + mark.getStudentId()));

            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setLesson(lesson);
            attendance.setStatus(mark.getStatus());

            saved.add(attendanceRepository.save(attendance));
        }

        return saved.stream()
                .map(this::toResponse)
                .toList();
    }

    /** Admin: "show today's Math attendance for Class 4B" -> find the Lesson, then this. */
    public List<AttendanceResponse> getAttendanceForLesson(Long lessonId) {
        return attendanceRepository.findByLesson_LessonId(lessonId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Admin: "show Jane's attendance across all subjects/lessons." */
    public List<AttendanceResponse> getAttendanceForStudent(Long studentId) {
        return attendanceRepository.findByStudent_StudentId(studentId).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Admin: "show Jane's attendance in Science specifically." */
    public List<AttendanceResponse> getAttendanceForStudentInSubject(Long studentId, Long subjectId) {
        return attendanceRepository.findByStudent_StudentIdAndLesson_Subject_SubjectId(studentId, subjectId).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Single mapping point: every method above funnels through here, so if
     * AttendanceResponse's shape ever changes, this is the only place to edit.
     */
    private AttendanceResponse toResponse(Attendance attendance) {
        AttendanceResponse response = new AttendanceResponse();

        response.setAttendanceId(attendance.getAttendanceId());
        response.setStatus(attendance.getStatus());

        Student student = attendance.getStudent();
        if (student != null) {
            response.setStudentId(student.getStudentId());   // was student.getId()
            response.setStudentName(student.getFirstName() + " " + student.getLastName());
        }

        Lesson lesson = attendance.getLesson();
        if (lesson != null) {
            response.setLessonId(lesson.getLessonId());
            response.setLessonDate(lesson.getDate());
            if (lesson.getSubject() != null) {
                response.setSubjectName(lesson.getSubject().getName());
            }
            if (lesson.getClasses() != null) {
                response.setClassName(lesson.getClasses().getName());
            }
        }

        return response;
    }
}