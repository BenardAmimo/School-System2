package com.school.service;

import com.school.error.SubjectNotFoundException;
import com.school.request.AssignmentRequest;
import com.school.response.AssignmentResponse;

import java.util.List;

public interface AssignServe {
    AssignmentResponse assignSubject(AssignmentRequest request) throws SubjectNotFoundException;

    AssignmentResponse getAssignmentById(Long assignmentId);

    List<AssignmentResponse> getAll();

    AssignmentResponse updateAssignment(Long assignmentId, AssignmentRequest assignmentRequest);

    void deleteAssignmentById(Long assignmentId);


}
