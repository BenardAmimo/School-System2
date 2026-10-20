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

        Teacher teacher = teacherRepo.findById(request.teacherId()).
                orElseThrow(()->new TeacherNotFoundException("Teacher not found!"));

        Subject subject = subjectRepository.findById(request.subjectId()).
                orElseThrow(()->new SubjectNotFoundException("Course not Found!"));

        SchoolClasses classes = schoolClassesRepository.findById(request.classesId())
                .orElseThrow(()->new SchoolClassesNotFoundException("Classes Not Found"));

        Assignment assignment = new Assignment();

        assignment.setTeacher(teacher);
        assignment.setSubject(subject);
        assignment.setClasses(classes);

        Assignment saved = repository.save(assignment);

       return AssignmentResponse.from(saved);
    }

    @Override
    public AssignmentResponse getAssignmentById(Long assignmentId) {
        Assignment assignment = repository.findById(assignmentId).
                orElseThrow(()->new RuntimeException("Assignment not Found"));

       return AssignmentResponse.from(assignment);
    }

    @Override
    public List<AssignmentResponse> getAll() {
          return repository.findAll()
                .stream()
                .map(AssignmentResponse::from)
                .toList();
    }

    @Override
    public AssignmentResponse updateAssignment(Long assignmentId, AssignmentRequest assignmentRequest) {
        Assignment assignDB = repository.findById(assignmentId).
                orElseThrow(()->new RuntimeException("Assignment not found in the System"));

        if(Objects.nonNull(assignmentRequest.subjectId())){
            Subject subject = subjectRepository.findById(assignmentRequest.subjectId()).
                    orElseThrow(()->new RuntimeException("Subject not found"));

            assignDB.setSubject(subject);
        }

        if(Objects.nonNull(assignmentRequest.teacherId())){
            Teacher teach = teacherRepo.findById(assignmentRequest.subjectId()).
                    orElseThrow(()->new RuntimeException("Teacher not found"));

            assignDB.setTeacher(teach);
        }

        if(Objects.nonNull(assignmentRequest.classesId())){
            SchoolClasses classes = schoolClassesRepository.findById(assignmentRequest.classesId()).
                    orElseThrow(()->new RuntimeException("Teacher not found"));

            assignDB.setClasses(classes);
        }
        Assignment saved = repository.save(assignDB);

        return AssignmentResponse.from(saved);
    }

    @Override
    public void deleteAssignmentById(Long assignmentId) {
        Assignment assignment = repository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        repository.delete(assignment);
    }


}
