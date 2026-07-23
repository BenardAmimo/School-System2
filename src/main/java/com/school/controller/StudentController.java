package com.school.controller;

import com.school.request.StudentRequest;
import com.school.response.MychildResponse;
import com.school.response.StudentResponse;
import com.school.service.StudentsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class StudentController {
    private final StudentsService studentsService;

    public StudentController(StudentsService studentsService) {
        this.studentsService = studentsService;
    }

    @PostMapping("/students")
    public ResponseEntity<StudentResponse> createStudent(@RequestBody StudentRequest studentRequest){
        StudentResponse response = studentsService.createStudents(studentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @GetMapping("/students")
    public ResponseEntity<List<StudentResponse>> getAllStudents(){
        List<StudentResponse> responses = studentsService.getAllStudents();
        return ResponseEntity.ok(responses);
    }


}
