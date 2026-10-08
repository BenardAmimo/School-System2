package com.school.service;

import com.school.entity.*;
import com.school.repo.AttendanceRepository;
import com.school.repo.LessonRepository;
import com.school.repo.StudentRepository;
import com.school.request.MarkAttendanceRequest;
import com.school.response.AttendanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final LessonRepository lessonRepository;
    private final StudentRepository studentRepository;
    private final StudentAccessPolicy accessPolicy;

    /**
     * Marks the register for one lesson. Re-submitting is safe: students already marked are skipped.
     * Each mark is checked: status present, no duplicate students in the request,
     * and the student must belong to the lesson's class.
     */
    @Transactional
    public List<AttendanceResponse> markAttendanceForLesson(Long lessonId,
                                                            List<MarkAttendanceRequest> marks,
                                                            Authentication auth) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));

        // TODO: if the caller is a TEACHER, require lesson.getTeacher() to be that teacher
        //       (compare by the logged-in email, as TeacherController.getMyClasses does).
        requireStaff(auth);

        if (marks == null || marks.isEmpty()) {
            throw new IllegalArgumentException("No attendance marks supplied");
        }

        List<Attendance> saved = new ArrayList<>();
        java.util.Set<Long> seen = new java.util.HashSet<>();

        for (MarkAttendanceRequest mark : marks) {
            if (mark.getStudentId() == null || mark.getStatus() == null) {
                throw new IllegalArgumentException("Each mark needs a studentId and a status");
            }
            if (!seen.add(mark.getStudentId())) {
                throw new IllegalArgumentException("Student listed twice: " + mark.getStudentId());
            }
            if (attendanceRepository.existsByStudent_StudentIdAndLesson_LessonId(mark.getStudentId(), lessonId)) {
                continue; // already marked
            }

            Student student = studentRepository.findById(mark.getStudentId())
                    .orElseThrow(() -> new IllegalArgumentException("Student not found: " + mark.getStudentId()));

            // A student can only be marked in a lesson for their own class.
            // Adjust getClasses()/getClassesId() if your entity names differ.
            if (lesson.getClasses() == null || student.getClasses() == null
                    || !Objects.equals(lesson.getClasses().getClassesId(), student.getClasses().getClassesId())) {
                throw new IllegalArgumentException("Student " + mark.getStudentId() + " is not in this lesson's class");
            }

            Attendance attendance = new Attendance();
            attendance.setStudent(student);
            attendance.setLesson(lesson);
            attendance.setStatus(mark.getStatus());
            saved.add(attendanceRepository.save(attendance));
        }

        return saved.stream().map(this::toResponse).toList();
    }

    /** Staff only: the whole lesson register. */
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceForLesson(Long lessonId, Authentication authentication) {
        requireStaff(authentication);
        return attendanceRepository.findByLesson_LessonId(lessonId).stream().map(this::toResponse).toList();
    }

    /** Admin, teacher, or the student's own parent. */
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceForStudent(Long studentId, Authentication authentication) {
        accessPolicy.assertCanView(studentId, authentication, true);
        return attendanceRepository.findByStudent_StudentId(studentId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAttendanceForStudentInSubject(Long studentId, Long subjectId, Authentication authentication) {
        accessPolicy.assertCanView(studentId, authentication, true);
        return attendanceRepository.findByStudent_StudentIdAndLesson_Subject_SubjectId(studentId, subjectId)
                .stream().map(this::toResponse).toList();
    }

    private void requireStaff(Authentication auth) {
        boolean staff = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals("ROLE_TEACHER") || r.equals("ROLE_ADMIN") || r.equals("ROLE_SUPER_ADMIN"));
        if (!staff) throw new AccessDeniedException("Staff only");
    }

    private AttendanceResponse toResponse(Attendance attendance) {
        AttendanceResponse response = new AttendanceResponse();
        response.setAttendanceId(attendance.getAttendanceId());
        response.setStatus(attendance.getStatus());

        Student student = attendance.getStudent();
        if (student != null) {
            response.setStudentId(student.getStudentId());
            response.setStudentName(student.getFirstName() + " " + student.getLastName());
        }

        Lesson lesson = attendance.getLesson();
        if (lesson != null) {
            response.setLessonId(lesson.getLessonId());
            response.setLessonDate(lesson.getDate());
            if (lesson.getSubject() != null) response.setSubjectName(lesson.getSubject().getName());
            if (lesson.getClasses() != null) response.setClassName(lesson.getClasses().getName());
        }
        return response;
    }
}