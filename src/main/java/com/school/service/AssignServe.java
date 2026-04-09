package com.school.service;

import com.school.request.AssignmentRequest;
import com.school.response.AssignmentResponse;

import java.util.List;

public interface AssignServe {
    AssignmentResponse assignCourse(AssignmentRequest request);

    AssignmentResponse getAssignmentById(Long assignmentId);

    List<AssignmentResponse> getAll();
}
