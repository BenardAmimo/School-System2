package com.school.service;

import com.example.demo.config.error.CourseNotFoundException;
import com.school.request.AssignmentRequest;
import com.school.response.AssignmentResponse;

import java.util.List;

public interface AssignServe {
    AssignmentResponse assignCourse(AssignmentRequest request) throws CourseNotFoundException;

    AssignmentResponse getAssignmentById(Long assignmentId);

    List<AssignmentResponse> getAll();

    AssignmentResponse updateAssignment(Long assignmentId, AssignmentRequest assignmentRequest);

    void deleteAssignmentById(Long assignmentId);


}
