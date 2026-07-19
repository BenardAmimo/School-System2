package com.school.service;

import com.school.request.StudentRequest;
import com.school.response.StudentResponse;

public interface StudentsServ {
    StudentResponse createStudents(StudentRequest studentRequest);
}
