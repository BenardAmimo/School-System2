package com.school.service;

import com.example.demo.config.error.StudentNotFoundtException;
import com.school.entity.Course;
import com.school.entity.Student;
import com.school.entity.Teacher;
import com.school.repo.CourseRepo;
import com.school.repo.StudentRepo;
import com.school.repo.TeacherRepo;
import com.school.request.StudentRequest;
import com.school.response.StudentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService implements StudentServe {
    private final StudentRepo studentRepo;

    @Override
    public StudentResponse creatStudent(StudentRequest studentRequest) {
        Student student = new Student();
        student.setName(studentRequest.getName());
        student.setEmail(studentRequest.getEmail());

        Student stud = studentRepo.save(student);

        StudentResponse respo = new StudentResponse();
        respo.setStudentId(stud.getStudentId());
        respo.setName(stud.getName());
        respo.setEmail(stud.getEmail());
        return respo;
    }

    @Override
    public List<StudentResponse> getAllStudents() {
        return studentRepo.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private StudentResponse mapToDto(Student student) {
        StudentResponse stude = new StudentResponse();
        stude.setStudentId(student.getStudentId());
        stude.setName(student.getName());
        stude.setEmail(student.getEmail());

        return stude;
    }

    @Override
    public StudentResponse getStudentById(Long studentId) {
        Student student = studentRepo.findById(studentId).
                orElseThrow(()->new RuntimeException("Student not found!"));

        StudentResponse studentResponse = new StudentResponse();
        studentResponse.setStudentId(student.getStudentId());
        studentResponse.setName(student.getName());
        studentResponse.setEmail(student.getEmail());

        return studentResponse;
    }

    @Override
    public StudentResponse updateStudent(Long studentId, StudentRequest studentRequest) {
        return null;
    }

    @Override
    public StudentResponse getStudentByName(String name) {
        return null;
    }

    @Override
    public StudentResponse deleteStudent(Long studentId) {
        return null;
    }

}
