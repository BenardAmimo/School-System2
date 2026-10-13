package com.school.service;

import com.school.entity.SchoolClasses;
import com.school.error.SchoolClassesNotFoundException;
import com.school.error.SubjectNotFoundException;
import com.school.error.TeacherNotFoundException;
import com.school.entity.Assignment;
import com.school.entity.Subject;
import com.school.entity.Teacher;
import com.school.repo.AssignmentRepository;
import com.school.repo.SchoolClassesRepository;
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
    private final SchoolClassesRepository schoolClassesRepository;

    @Override
    public AssignmentResponse assignSubject(AssignmentRequest request) throws SubjectNotFoundException {

        Teacher teacher = teacherRepo.findById(request.getTeacherId()).
                orElseThrow(()->new TeacherNotFoundException("Teacher not found!"));

        Subject subject = subjectRepository.findById(request.getSubjectId()).
                orElseThrow(()->new SubjectNotFoundException("Course not Found!"));

        SchoolClasses classes = schoolClassesRepository.findById(request.getClassesId())
                .orElseThrow(()->new SchoolClassesNotFoundException("Classes Not Found"));

        Assignment assignment = new Assignment();

        assignment.setTeacher(teacher);
        assignment.setSubject(subject);
        assignment.setClasses(classes);

        Assignment saved = repository.save(assignment);

        AssignmentResponse resp = new AssignmentResponse();
        resp.setAssignmentId(saved.getAssignmentId());
        resp.setSubjectName(saved.getSubject().getName());
        resp.setTeacherName(saved.getTeacher().getUserReg().getFirstName());
        resp.setTeacherName(saved.getTeacher().getUserReg().getLastName());
        resp.setClassesName(saved.getClasses().getName());

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
        assignmentResponse.setClassesName(assignment.getClasses().getName());

        return assignmentResponse;
    }
//mapper
    private AssignmentResponse toResponse(Assignment ca) {

        AssignmentResponse res = new AssignmentResponse();
        res.setAssignmentId(ca.getAssignmentId());
        res.setTeacherName(ca.getTeacher().getUserReg().getFirstName());
        res.setTeacherName(ca.getTeacher().getUserReg().getLastName());
        res.setSubjectName(ca.getSubject().getName());
        res.setClassesName(ca.getClasses().getName());
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
                    orElseThrow(()->new RuntimeException("Subject not found"));

            assignDB.setSubject(subject);
        }

        if(Objects.nonNull(assignmentRequest.getTeacherId())){
            Teacher teach = teacherRepo.findById(assignmentRequest.getSubjectId()).
                    orElseThrow(()->new RuntimeException("Teacher not found"));

            assignDB.setTeacher(teach);
        }

        if(Objects.nonNull(assignmentRequest.getClassesId())){
            SchoolClasses classes = schoolClassesRepository.findById(assignmentRequest.getClassesId()).
                    orElseThrow(()->new RuntimeException("Teacher not found"));

            assignDB.setClasses(classes);
        }
        Assignment saved = repository.save(assignDB);

        AssignmentResponse assRespo = new AssignmentResponse();

        assRespo.setAssignmentId(saved.getAssignmentId());
        assRespo.setSubjectName(saved.getSubject().getName());
        assRespo.setTeacherName(saved.getTeacher().getUserReg().getFirstName());
        assRespo.setTeacherName(saved.getTeacher().getUserReg().getLastName());
        assRespo.setClassesName(saved.getClasses().getName());
        return assRespo;
    }

    @Override
    public void deleteAssignmentById(Long assignmentId) {
        Assignment assignment = repository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        repository.delete(assignment);
    }


}
