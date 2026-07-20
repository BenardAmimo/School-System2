package com.school.controller;

import com.school.error.SubjectNotFoundException;
import com.school.request.AssignmentRequest;
import com.school.response.AssignmentResponse;
import com.school.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;

    @PostMapping("/assigns")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<AssignmentResponse> assignSubject(@RequestBody AssignmentRequest request) throws SubjectNotFoundException {
        AssignmentResponse assign = assignmentService.assignSubject(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(assign);
    }

    @GetMapping("/assign/id/{assignmentId}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(@PathVariable("assignmentId")Long assignmentId){
        AssignmentResponse assign = assignmentService.getAssignmentById(assignmentId);
        return ResponseEntity.ok(assign);
        }


    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAll() {

        return new ResponseEntity<>(assignmentService.getAll(),HttpStatus.OK);
    }
    @PutMapping("/assign/{assignmentId}")
    public ResponseEntity<AssignmentResponse> updateAssignment(@PathVariable("assignmentId")Long assignmentId,
                                                               @RequestBody AssignmentRequest assignmentRequest){
        AssignmentResponse assign = assignmentService.updateAssignment(assignmentId,assignmentRequest);

        return new ResponseEntity<>(assign, HttpStatus.CREATED);
    }

    @DeleteMapping("/assign/id/{assignmentId}")
    public ResponseEntity<String> deleteAssignmentById(@PathVariable("assignmentId")Long assignmentId){
        assignmentService.deleteAssignmentById(assignmentId);
        return new ResponseEntity<>("Successfully deleted",HttpStatus.OK);
    }


}
