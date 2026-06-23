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
import com.school.security.entity.UserReg;
import com.school.security.repository.UserRepository;
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
    private final UserRepository userRepository;


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
        stude.setFirstName(student.getUserReg().getLastName());
        stude.setLastName(student.getUserReg().getFirstName());
        stude.setEmail(student.getUserReg().getEmail());

        return stude;
    }

    @Override
    public StudentResponse getStudentById(Long studentId) {
        Student student = studentRepo.findById(studentId).
                orElseThrow(()->new RuntimeException("Student not found!"));

        StudentResponse studentResponse = new StudentResponse();
        studentResponse.setStudentId(student.getStudentId());
        studentResponse.setFirstName(student.getUserReg().getFirstName());
        studentResponse.setFirstName(student.getUserReg().getFirstName());
        studentResponse.setEmail(student.getUserReg().getEmail());

        return studentResponse;
    }

    @Override
    public StudentResponse updateStudent(Long studentId, StudentRequest studentRequest) {
        Student studentDB = studentRepo.findById(studentId).
                orElseThrow(()->new RuntimeException("Student Not available!"));

        UserReg user = userRepository.findById((studentRequest.getUserId()))
                .orElseThrow(()->new RuntimeException("User not found"));

        if(Objects.nonNull(studentRequest.getRegNo())&&!"".equalsIgnoreCase(studentRequest.getRegNo())){
            studentDB.setRegNo(studentRequest.getRegNo());
        }

        /* if(Objects.nonNull(studentRequest.getEmail())&&!"".equalsIgnoreCase(studentRequest.getEmail())){
            studentDB.setEmail(studentRequest.getEmail());
        }*/
        studentDB.setUserReg(user);
        Student stud = studentRepo.save(studentDB);

        StudentResponse respo = new StudentResponse();

                respo.setStudentId(stud.getStudentId());
                respo.setFirstName(stud.getUserReg().getFirstName());
                respo.setLastName(stud.getUserReg().getLastName());
                respo.setEmail(stud.getUserReg().getEmail());

        return respo;
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
