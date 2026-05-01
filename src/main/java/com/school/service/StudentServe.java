package com.school.service;

import com.school.request.StudentRequest;
import com.school.response.StudentResponse;

import java.util.List;
public interface StudentServe {

    StudentResponse creatStudent(StudentRequest studentRequest);

    List<StudentResponse> getAllStudents();

    StudentResponse getStudentById(Long studentId);

    StudentResponse updateStudent(Long studentId, StudentRequest studentRequest);

    StudentResponse getStudentByName(String name);

    StudentResponse deleteStudent(Long studentId);

}
