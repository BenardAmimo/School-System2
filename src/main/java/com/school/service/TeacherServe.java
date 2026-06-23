package com.school.service;

import com.school.entity.Teacher;
import com.school.request.TeacherRequest;
import com.school.response.TeacherResponse;

import java.util.List;

public interface TeacherServe {

    TeacherResponse getTeacherByName(String name);

    TeacherResponse getTeacherByid(Long teacherId);

    List<TeacherResponse> getAllTeachers();

    TeacherResponse updateTeacher(Long teacherId, TeacherRequest teacherRequest);
}
