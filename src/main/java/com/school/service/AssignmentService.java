package com.school.service;

import com.school.entity.Assignment;
import com.school.entity.Course;
import com.school.entity.Teacher;
import com.school.repo.AssignmentRepository;
import com.school.repo.CourseRepo;
import com.school.repo.TeacherRepo;
import com.school.request.AssignmentRequest;
import com.school.response.AssignmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AssignmentService implements AssignServe{
    private final AssignmentRepository repository;
    private final TeacherRepo teacherRepo;
    private final CourseRepo courseRepo;

    @Override
    public AssignmentResponse assignCourse(AssignmentRequest request){

        Teacher teacher = teacherRepo.findById(request.getTeacherId()).
                orElseThrow(()->new RuntimeException("Teacher not found!"));

        Course course = courseRepo.findById(request.getCourseId()).
                orElseThrow(()->new RuntimeException("Course nt Found!"));

        Assignment assignment = new Assignment();

        assignment.setTeacher(teacher);
        assignment.setCourse(course);

        Assignment saved = repository.save(assignment);

        AssignmentResponse resp = new AssignmentResponse();
        resp.setAssignmentId(saved.getAssignmentId());
        resp.setCourseName(saved.getCourse().getName());
        resp.setTeacherName(saved.getTeacher().getName());

        return resp;
    }

    @Override
    public AssignmentResponse getAssignmentById(Long assignmentId) {
        Assignment assignment = repository.findById(assignmentId).
                orElseThrow(()->new RuntimeException("Assignment not Found"));

        AssignmentResponse assignmentResponse = new AssignmentResponse();

        assignmentResponse.setAssignmentId(assignment.getAssignmentId());
        assignmentResponse.setTeacherName(assignment.getTeacher().getName());
        assignmentResponse.setCourseName(assignment.getCourse().getName());

        return assignmentResponse;
    }
//mapper
    private AssignmentResponse toResponse(Assignment ca) {

        AssignmentResponse res = new AssignmentResponse();
        res.setAssignmentId(ca.getAssignmentId());
        res.setTeacherName(ca.getTeacher().getName());
        res.setCourseName(ca.getCourse().getName());
        return res;
    }

    @Override
    public List<AssignmentResponse> getAll() {
          return repository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public AssignmentResponse updateAssignment(Long assignmentId, AssignmentRequest assignmentRequest) {
        Assignment assignDB = repository.findById(assignmentId).
                orElseThrow(()->new RuntimeException("Assignment not found in the System"));

        if(Objects.nonNull(assignmentRequest.getCourseId())){
            Course course = courseRepo.findById(assignmentRequest.getCourseId()).
                    orElseThrow(()->new RuntimeException("course not found"));

            assignDB.setCourse(course);
        }

        if(Objects.nonNull(assignmentRequest.getTeacherId())){
            Teacher teach = teacherRepo.findById(assignmentRequest.getCourseId()).
                    orElseThrow(()->new RuntimeException("Teacher not found"));

            assignDB.setTeacher(teach);
        }
        Assignment saved = repository.save(assignDB);

        AssignmentResponse assRespo = new AssignmentResponse();

        assRespo.setAssignmentId(saved.getAssignmentId());
        assRespo.setCourseName(saved.getCourse().getName());
        assRespo.setTeacherName(saved.getTeacher().getName());
        return assRespo;
    }

    @Override
    public void deleteAssignmentById(Long assignmentId) {
        Assignment assignment = repository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        repository.delete(assignment);
    }
}
