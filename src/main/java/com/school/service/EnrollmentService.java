package com.school.service;

import com.school.entity.Assignment;
import com.school.entity.Enrollment;
import com.school.entity.Student;
import com.school.repo.AssignmentRepository;
import com.school.repo.EnrollmentRepoitory;
import com.school.repo.StudentRepo;
import com.school.request.EnrollmentRequest;
import com.school.response.EnrollmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService implements EnrollServe {
    private final EnrollmentRepoitory enrollmentRepoitory;
    private final StudentRepo studentRepo;
    private final AssignmentRepository assignmentRepository;
    @Override
    public EnrollmentResponse enrollStudent(EnrollmentRequest request) {
        Student student = studentRepo.findById(request.getStudentId()).
                orElseThrow(()->new RuntimeException("Student not Available!"));

        Assignment assignment = assignmentRepository.findById(request.getAssignmentId()).
                orElseThrow(()->new RuntimeException("Assigment not yet Done!"));

        Enrollment enrolling = new Enrollment();
        enrolling.setStudent(student);
        enrolling.setAssignment(assignment);

        Enrollment saving = enrollmentRepoitory.save(enrolling);

        EnrollmentResponse enr = new EnrollmentResponse();

        enr.setEnrollmentId(saving.getEnrollmentId());
        enr.setStudentName(saving.getStudent().getName());
        enr.setCourseName(saving.getAssignment().getCourse().getName());
        enr.setTeacherName(saving.getAssignment().getTeacher().getName());

        return enr;
    }

    @Override
    public EnrollmentResponse getEnrollments(Long enrollmentId) {

        Enrollment enrollment = enrollmentRepoitory.findById(enrollmentId).
                orElseThrow(()->new RuntimeException("Enrollments not Available"));

        EnrollmentResponse enrollmentResponse = new EnrollmentResponse();

        enrollmentResponse.setEnrollmentId(enrollment.getEnrollmentId());
        enrollmentResponse.setTeacherName(enrollment.getAssignment().getTeacher().getName());
        enrollmentResponse.setCourseName(enrollment.getAssignment().getCourse().getName());
        enrollmentResponse.setStudentName(enrollment.getStudent().getName());


        return enrollmentResponse;
    }

    @Override
    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentRepoitory.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }
//mapper
    private EnrollmentResponse mapToDto(Enrollment enrollment) {
        EnrollmentResponse enrolls =  new EnrollmentResponse();
        enrolls.setEnrollmentId(enrollment.getEnrollmentId());
        enrolls.setStudentName(enrollment.getStudent().getName());
        enrolls.setCourseName(enrollment.getAssignment().getCourse().getName());
        enrolls.setTeacherName(enrollment.getAssignment().getTeacher().getName());

        return enrolls;
    }
}
