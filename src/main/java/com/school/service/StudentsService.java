package com.school.service;

import com.school.entity.Parent;
import com.school.entity.SchoolClasses;
import com.school.entity.Student;
import com.school.entity.TimetableSlot;
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
        Parent parent = parentRepo.findById(studentRequest.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent not found"));

        SchoolClasses classes = schoolClassesRepository.findById(studentRequest.getClassesId())
                .orElseThrow(() -> new RuntimeException("No class available"));

        Student student = new Student();
        student.setFirstName(studentRequest.getFirstName());
        student.setLastName(studentRequest.getLastName());
        student.setAge(studentRequest.getAge());
        student.setGender(studentRequest.getGender());
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

        if (Objects.nonNull(request.getFirstName()) && !request.getFirstName().isBlank()) {
            studentDB.setFirstName(request.getFirstName());
        }

        if (Objects.nonNull(request.getLastName()) && !request.getLastName().isBlank()) {
            studentDB.setLastName(request.getLastName());
        }

        if (Objects.nonNull(request.getGender())) {
            studentDB.setGender(request.getGender());
        }

        if (Objects.nonNull(request.getAge())) {
            studentDB.setAge(request.getAge());
        }

        if (Objects.nonNull(request.getParentId())) {
            Parent parent = parentRepo.findById(request.getParentId())
                    .orElseThrow(() -> new RuntimeException("No Parent Found"));
            studentDB.setParent(parent);
        }

        if (Objects.nonNull(request.getClassesId())) {
            SchoolClasses classes = schoolClassesRepository.findById(request.getClassesId())
                    .orElseThrow(() -> new RuntimeException("No classes"));
            studentDB.setClasses(classes);
        }
        Student stud = studentRepository.save(studentDB);

        return toResponse(stud);
    }

    private MychildResponse toChildResponse(Student student) {
        SchoolClasses schoolClass = student.getClasses();

        List<StudentSubjectSummary> subjects = schoolClass == null
                ? List.of()
                : schoolClass.getSubjects().stream()
                .map(subject -> {
                    String teacherName = subject.getAssignment().isEmpty()
                            ? "Unassigned"
                            : subject.getAssignment().get(0).getTeacher().getUserReg().getFirstName()
                            + " " + subject.getAssignment().get(0).getTeacher().getUserReg().getLastName();

                    return StudentSubjectSummary.builder()
                            .subjectId(subject.getSubjectId())
                            .subjectName(subject.getName())
                            .teacherName(teacherName)
                            .build();
                })
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

}