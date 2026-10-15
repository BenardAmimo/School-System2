package com.school.service;

import com.school.entity.*;
import com.school.repo.ParentRepo;
import com.school.repo.SchoolClassesRepository;
import com.school.repo.StudentRepository;
import com.school.request.StudentRequest;
import com.school.response.MychildResponse;
import com.school.response.StudentResponse;
import com.school.response.StudentSubjectSummary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
        Parent parent = parentRepo.findById(studentRequest.parentId())
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        SchoolClasses classes = schoolClassesRepository.findById(studentRequest.classesId())
                .orElseThrow(() -> new RuntimeException("No class available"));

        Student student = new Student();
        student.setFirstName(studentRequest.firstName());
        student.setLastName(studentRequest.lastName());
        student.setAge(studentRequest.age());
        student.setGender(studentRequest.gender());
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

    @Override
    public List<MychildResponse> getMyChildren(String email) {
        Parent parent = parentRepo.findByUserReg_Email(email)
                .orElseThrow(() -> new RuntimeException("This account is not linked to a parent record"));

        return parent.getStudent().stream()
                .map(this::toChildResponse)
                .toList();
    }

    @Override
    public void deleteStudent(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        studentRepository.delete(student);
    }

    @Override
    public StudentResponse updateStudent(Long studentId, StudentRequest request) {
        Student studentDB = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (Objects.nonNull(request.firstName()) && !request.firstName().isBlank()) {
            studentDB.setFirstName(request.firstName());
        }

        if (Objects.nonNull(request.lastName()) && !request.lastName().isBlank()) {
            studentDB.setLastName(request.lastName());
        }

        if (Objects.nonNull(request.gender())) {
            studentDB.setGender(request.gender());
        }

        if (Objects.nonNull(request.age())) {
            studentDB.setAge(request.age());
        }

        if (Objects.nonNull(request.parentId())) {
            Parent parent = parentRepo.findById(request.parentId())
                    .orElseThrow(() -> new RuntimeException("No Parent Found"));
            studentDB.setParent(parent);
        }

        if (Objects.nonNull(request.classesId())) {
            SchoolClasses classes = schoolClassesRepository.findById(request.classesId())
                    .orElseThrow(() -> new RuntimeException("No classes"));
            studentDB.setClasses(classes);
        }
        Student stud = studentRepository.save(studentDB);

        return toResponse(stud);
    }
    private MychildResponse toChildResponse(Student student) {
        SchoolClasses schoolClass = student.getClasses();

        List<StudentSubjectSummary> subjects =
                (schoolClass == null || schoolClass.getAssignments() == null)
                        ? List.of()
                        : schoolClass.getAssignments().stream()
                        .filter(a -> a.getSubject() != null)
                        .map(a -> StudentSubjectSummary.builder()
                                .subjectId(a.getSubject().getSubjectId())
                                .subjectName(a.getSubject().getName())
                                .teacherName(teacherName(a.getTeacher()))
                                .build())
                        .toList();

        return MychildResponse.builder()
                .studentId(student.getStudentId())
                .firstName(student.getFirstName())
                .lastName(student.getLastName())
                .gender(student.getGender())
                .age(student.getAge())
                .className(schoolClass != null ? schoolClass.getName() : "Unassigned")
                .classLocation(schoolClass != null ? schoolClass.getLocation() : null)
                .subjects(subjects)
                .build();
    }

    private StudentResponse toResponse(Student student) {
        StudentResponse respond = new StudentResponse();
        respond.setStudentId(student.getStudentId());
        respond.setFirstName(student.getFirstName());
        respond.setLastName(student.getLastName());
        respond.setAge(student.getAge());
        respond.setGender(student.getGender());
        respond.setParentFirstName(student.getParent().getUserReg().getFirstName());
        respond.setParentLastName(student.getParent().getUserReg().getLastName());
        respond.setClassName(student.getClasses().getName());
        List<String> teacherNames = student.getClasses().getTimetableSlots().stream()
                .map(TimetableSlot::getTeacher)
                .distinct()
                .map(t -> t.getUserReg().getFirstName() + " " + t.getUserReg().getLastName())
                .toList();
        respond.setTeacherNames(teacherNames.isEmpty() ? List.of("Unassigned") : teacherNames);

        return respond;
    }

    private String teacherName(Teacher teacher) {
        if (teacher == null || teacher.getUserReg() == null) return "Unassigned";
        return teacher.getUserReg().getFirstName() + " " + teacher.getUserReg().getLastName();
    }

}