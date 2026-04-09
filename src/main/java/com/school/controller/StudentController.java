package com.school.controller;

import com.school.entity.Student;
import com.school.request.StudentRequest;
import com.school.response.StudentResponse;
import com.school.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@Slf4j
public class StudentController {
    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @PostMapping("/students")
    public ResponseEntity<StudentResponse> creatStudent(@RequestBody StudentRequest studentRequest){

        log.info("Saving new student with name: {} ",studentRequest.getName());
        StudentResponse creating = studentService.creatStudent(studentRequest);
        log.info("Student saved with id {} ",creating.getStudentId());
        return ResponseEntity.status(201).body(creating);
    }


    @GetMapping("/students")
    public ResponseEntity<List<StudentResponse>> getAllStudents(){
        log.info("Get mapping of all students");
        List<StudentResponse> students = studentService.getAllStudents();

        return ResponseEntity.ok(students);
    }


    @GetMapping("/student/id/{studentId}")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable("studentId") Long studentId){
        log.info("Get request for student with id {} ",studentId);
        StudentResponse student = studentService.getStudentById(studentId);
        log.info("Student found with id {} ",student.getStudentId());
        return ResponseEntity.ok(student);
    }

    @PutMapping("/student/update/{studentId}")
    public ResponseEntity<StudentResponse> updateStudent(@PathVariable("studentId")Long studentId,
                                                 @RequestBody StudentRequest studentRequest){
        log.info("Put Request for student: {} ",studentRequest.getName());
        StudentResponse updating = studentService.updateStudent(studentId,studentRequest);
        log.info("Updated student with id {} ",updating.getStudentId());

        return ResponseEntity.status(202).body(updating);
    }


    @GetMapping("/student/name/{name}")
    public ResponseEntity<StudentResponse>getStudentByName(@PathVariable("name")String name){
        log.info("Get Request for student: {} ",name);
        StudentResponse student = studentService.getStudentByName(name);
        log.info("Student with id {} found",student.getStudentId());
        return ResponseEntity.ok(student);
    }


    @DeleteMapping("/student/id/{studentId}")
    public ResponseEntity<String> deleteStudent(@PathVariable("studentId")Long studentId){
        log.info("Delete Request for student id {} ",studentId);
            StudentResponse stud = studentService.deleteStudent(studentId);
            log.info("student {} successfully deleted",stud.getStudentId());
        return ResponseEntity.ok("Successfully deleted");
    }
}
