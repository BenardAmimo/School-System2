package com.school.controller;

import com.school.request.StudentRequest;
import com.school.response.StudentResponse;
import com.school.service.StudentsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api")
public class StudentController {
    private final StudentsService studentsService;

    public StudentController(StudentsService studentsService) {
        this.studentsService = studentsService;
    }

    @PostMapping("/Students")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<StudentResponse> createStudent(@RequestBody StudentRequest studentRequest){
        StudentResponse response = studentsService.createStudents(studentRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
}
