package com.school.controller;

import com.school.repo.EnrollmentRepoitory;
import com.school.request.EnrollmentRequest;
import com.school.response.EnrollmentResponse;
import com.school.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentService enrollmentService;


    @PostMapping("/enrolls")
    public ResponseEntity<EnrollmentResponse> enrollStudent(@RequestBody EnrollmentRequest request){
        EnrollmentResponse enroll = enrollmentService.enrollStudent(request);
        return ResponseEntity.status(201).body(enroll);
    }

    @GetMapping("/enroll/id/{enrollmentId}")
    public ResponseEntity<EnrollmentResponse> getEnrollments(@PathVariable("enrollmentId")Long enrollmentId){
        EnrollmentResponse enrolls = enrollmentService.getEnrollments(enrollmentId);
        return ResponseEntity.ok(enrolls);
    }
    @GetMapping("/enrollments")
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments(){
        List<EnrollmentResponse> getEnrolls = enrollmentService.getAllEnrollments();
        return ResponseEntity.ok(getEnrolls);
    }
}
