package com.school.service;

import com.school.entity.SchoolClasses;
import com.school.entity.Student;
import com.school.repo.SchoolClassesRepository;
import com.school.repo.StudentRepository;
import com.school.request.StudentRequest;
import com.school.response.StudentResponse;

public class StudentsService implements StudentsServ{
    private final StudentRepository studentRepository;
    private final SchoolClassesRepository repository;

    public StudentsService(StudentRepository studentRepository, SchoolClassesRepository repository) {
        this.studentRepository = studentRepository;
        this.repository = repository;
    }

    @Override
    public StudentResponse createStudents(StudentRequest studentRequest) {


        Student student = new Student();
        student.setFirstName(studentRequest.getFirstName());
        student.setLastName(studentRequest.getLastName());


        Student saved = studentRepository.save(student);

        StudentResponse studentResponse = new StudentResponse();
        studentResponse.setStudentId(saved.getStudentId());
        studentResponse.setFirstName(saved.getFirstName());
        studentResponse.setLastName(saved.getLastName());

        return studentResponse;
    }
}
