package com.school.service;

import com.school.request.EnrollmentRequest;
import com.school.response.EnrollmentResponse;

import java.util.List;

public interface EnrollServe {
    EnrollmentResponse enrollStudent(EnrollmentRequest request);

    EnrollmentResponse getEnrollments(Long enrollmentId);

    List<EnrollmentResponse> getAllEnrollments();
}
