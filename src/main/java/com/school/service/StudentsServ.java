package com.school.service;

import com.school.request.StudentRequest;
import com.school.response.StudentResponse;

import java.util.List;

public interface StudentsServ {
    StudentResponse createStudents(StudentRequest studentRequest);

    List<StudentResponse> getAllStudents();
}
