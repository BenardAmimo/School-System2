package com.school.controller;

import com.school.request.AssignmentRequest;
import com.school.response.AssignmentResponse;
import com.school.service.AssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AssignmentController {
    private final AssignmentService assignmentService;

    @PostMapping("/assigns")
    public ResponseEntity<AssignmentResponse> assignCourse(@RequestBody AssignmentRequest request){
        AssignmentResponse assign = assignmentService.assignCourse(request);

        return ResponseEntity.status(201).body(assign);
    }

    @GetMapping("/assign/id/{assignmentId}")
    public ResponseEntity<AssignmentResponse> getAssignmentById(@PathVariable("assignmentId")Long assignmentId){
        AssignmentResponse assign = assignmentService.getAssignmentById(assignmentId);
        return ResponseEntity.ok(assign);
        }


    @GetMapping("/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAll() {

        return ResponseEntity.ok(assignmentService.getAll());
    }
    @PutMapping("/assign/{assignmentId}")
    public ResponseEntity<AssignmentResponse> updateAssignment(@PathVariable("assignmentId")Long assignmentId,
                                                               @RequestBody AssignmentRequest assignmentRequest){
        AssignmentResponse assign = assignmentService.updateAssignment(assignmentId,assignmentRequest);

        return ResponseEntity.status(202).body(assign);
    }
    @DeleteMapping("/assign/id/{assignmentId}")
    public ResponseEntity<String> deleteAssignmentById(@PathVariable("assignmentId")Long assignmentId){
        assignmentService.deleteAssignmentById(assignmentId);
        return ResponseEntity.ok("Successfully deleted");
    }
}
