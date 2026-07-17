package com.school.service;

import com.school.error.SubjectNotFoundException;
import com.school.error.TeacherNotFoundException;
import com.school.entity.Assignment;
import com.school.entity.Subject;
import com.school.entity.Teacher;
import com.school.repo.AssignmentRepository;
import com.school.repo.SubjectRepository;
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
    private final SubjectRepository subjectRepository;

    @Override
    public AssignmentResponse assignCourse(AssignmentRequest request) throws SubjectNotFoundException {

        Teacher teacher = teacherRepo.findById(request.getTeacherId()).
                orElseThrow(()->new TeacherNotFoundException("Teacher not found!"));

        Subject subject = subjectRepository.findById(request.getSubjectId()).
                orElseThrow(()->new SubjectNotFoundException("Course nt Found!"));

        Assignment assignment = new Assignment();

        assignment.setTeacher(teacher);
        assignment.setSubject(subject);

        Assignment saved = repository.save(assignment);

        AssignmentResponse resp = new AssignmentResponse();
        resp.setAssignmentId(saved.getAssignmentId());
        resp.setSubjectName(saved.getSubject().getName());
        resp.setTeacherName(saved.getTeacher().getUserReg().getFirstName());
        resp.setTeacherName(saved.getTeacher().getUserReg().getLastName());

        return resp;
    }

    @Override
    public AssignmentResponse getAssignmentById(Long assignmentId) {
        Assignment assignment = repository.findById(assignmentId).
                orElseThrow(()->new RuntimeException("Assignment not Found"));

        AssignmentResponse assignmentResponse = new AssignmentResponse();

        assignmentResponse.setAssignmentId(assignment.getAssignmentId());
        assignmentResponse.setTeacherName(assignment.getTeacher().getUserReg().getFirstName());
        assignmentResponse.setTeacherName(assignment.getTeacher().getUserReg().getLastName());
        assignmentResponse.setSubjectName(assignment.getSubject().getName());

        return assignmentResponse;
    }
//mapper
    private AssignmentResponse toResponse(Assignment ca) {

        AssignmentResponse res = new AssignmentResponse();
        res.setAssignmentId(ca.getAssignmentId());
        res.setTeacherName(ca.getTeacher().getUserReg().getFirstName());
        res.setTeacherName(ca.getTeacher().getUserReg().getLastName());
        res.setSubjectName(ca.getSubject().getName());
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

        if(Objects.nonNull(assignmentRequest.getSubjectId())){
            Subject subject = subjectRepository.findById(assignmentRequest.getSubjectId()).
                    orElseThrow(()->new RuntimeException("course not found"));

            assignDB.setSubject(subject);
        }

        if(Objects.nonNull(assignmentRequest.getTeacherId())){
            Teacher teach = teacherRepo.findById(assignmentRequest.getSubjectId()).
                    orElseThrow(()->new RuntimeException("Teacher not found"));

            assignDB.setTeacher(teach);
        }
        Assignment saved = repository.save(assignDB);

        AssignmentResponse assRespo = new AssignmentResponse();

        assRespo.setAssignmentId(saved.getAssignmentId());
        assRespo.setSubjectName(saved.getSubject().getName());
        assRespo.setTeacherName(saved.getTeacher().getUserReg().getFirstName());
        assRespo.setTeacherName(saved.getTeacher().getUserReg().getLastName());
        return assRespo;
    }

    @Override
    public void deleteAssignmentById(Long assignmentId) {
        Assignment assignment = repository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        repository.delete(assignment);
    }


}
