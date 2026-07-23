package com.school.service;

import com.school.entity.Parent;
import com.school.entity.SchoolClasses;
import com.school.entity.Student;
import com.school.entity.Teacher;
import com.school.repo.ParentRepo;
import com.school.repo.SchoolClassesRepository;
import com.school.repo.StudentRepository;
import com.school.request.StudentRequest;
import com.school.response.StudentResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentsService implements StudentsServ {
    private final StudentRepository studentRepository;
    private final ParentRepo parentRepo;
    private final SchoolClassesRepository schoolClassesRepository;

    public StudentsService(StudentRepository studentRepository, ParentRepo parentRepo, SchoolClassesRepository schoolClassesRepository) {
        this.studentRepository = studentRepository;
        this.parentRepo = parentRepo;
        this.schoolClassesRepository = schoolClassesRepository;
    }

    @Override
    public StudentResponse createStudents(StudentRequest studentRequest) {
        Parent parent = parentRepo.findById(studentRequest.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        SchoolClasses classes = schoolClassesRepository.findById(studentRequest.getClassesId())
                .orElseThrow(() -> new RuntimeException("No class available"));

        Student student = new Student();
        student.setFirstName(studentRequest.getFirstName());
        student.setLastName(studentRequest.getLastName());
        student.setParent(parent);
        student.setClasses(classes);

        Student saved = studentRepository.save(student);
        return toResponse(saved);
    }

    @Override
    public List<StudentResponse> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private StudentResponse toResponse(Student student) {
        StudentResponse respond = new StudentResponse();
        respond.setStudentId(student.getStudentId());
        respond.setFirstName(student.getFirstName());
        respond.setLastName(student.getLastName());
        respond.setParentFirstName(student.getParent().getUserReg().getFirstName());
        respond.setParentLastName(student.getParent().getUserReg().getLastName());
        respond.setClassName(student.getClasses().getName());

        List<String> teacherNames = student.getClasses().getTeachers().stream()
                .map(t -> t.getUserReg().getFirstName() + " " + t.getUserReg().getLastName())
                .toList();
        respond.setTeacherNames(teacherNames.isEmpty() ? List.of("Unassigned") : teacherNames);

        return respond;
    }
}