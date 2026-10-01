package com.school.service;

import com.school.request.AttendanceRequest;
import com.school.request.MarkAttendanceRequest;
import com.school.response.AttendanceResponse;

import java.util.List;

public interface AttendanceServing {

    List<AttendanceResponse> markAttendanceForLesson(Long lessonId, List<MarkAttendanceRequest> marks);

    List<AttendanceResponse> getAttendanceForLesson(Long lessonId);

    List<AttendanceResponse> getAttendanceForStudent(Long studentId);

    List<AttendanceResponse> getAttendanceForStudentInSubject(Long studentId, Long subjectId);
}
