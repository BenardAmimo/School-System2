package com.school.service;

import com.school.entity.SchoolClasses;
import com.school.entity.Student;
import com.school.entity.Subject;
import com.school.repo.SchoolClassesRepository;
import com.school.repo.StudentRepository;
import com.school.repo.SubjectRepository;
import com.school.request.SubjectRequest;
import com.school.response.SubjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubjectService implements SubjectServe {
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;
    private final SchoolClassesRepository schoolClassesRepository;

    @Override
    public SubjectResponse createSubject(SubjectRequest subjectRequest) {
        Student student = studentRepository.findById(subjectRequest.getStudentId())
                .orElseThrow(()->new RuntimeException("Student not in the System"));
        SchoolClasses classes = schoolClassesRepository.findById(subjectRequest.getClassesId())
                .orElseThrow(()->new RuntimeException("No classes Available"));
        Subject subject = new Subject();
        subject.setName(subjectRequest.getName());
        subject.setStudent(student);
        subject.setSchoolClasses(classes);
        subject.setDescription(subjectRequest.getDescription());

        Subject saved = subjectRepository.save(subject);

        SubjectResponse response = new SubjectResponse();
        response.setSubjectId(saved.getSubjectId());
        response.setName(saved.getName());
        response.setDescription(saved.getDescription());
        response.setStudentsFirstName(saved.getStudent().getFirstName());
        response.setStudentsLastName(saved.getStudent().getLastName());
        response.setClassesName(saved.getSchoolClasses().getName());

        return response;

    }


    @Override
    public List<SubjectResponse> fetchAllSubjects() {
        return subjectRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private SubjectResponse mapToDto(Subject subject) {
        SubjectResponse res = new SubjectResponse();
        res.setSubjectId(subject.getSubjectId());
        res.setName(subject.getName());
        res.setDescription(subject.getDescription());

        return res;
    }

    @Override
    public SubjectResponse getBySubjectName(String name) {
        Subject subjects = subjectRepository.findByNameIgnoreCase(name);

        SubjectResponse resp = new SubjectResponse();

        resp.setSubjectId(subjects.getSubjectId());
        resp.setName(subjects.getName());
        resp.setDescription(subjects.getDescription());
        return resp;
    }

    @Override
    public void deleteSubjectById(Long subjectId) {

        Subject subject = subjectRepository.findById(subjectId).
                orElseThrow(()->new RuntimeException("Subject Not found"));

        subjectRepository.delete(subject);
    }

    @Override
    public void deleteAllSubjects() {
       subjectRepository.deleteAll();
    }

    @Override
    public SubjectResponse getByIdSubjectId(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId).
                orElseThrow(() -> new RuntimeException("Course Not found"));

        SubjectResponse courseResponse = new SubjectResponse();

        courseResponse.setSubjectId(subject.getSubjectId());;
        courseResponse.setName(subject.getName());
        courseResponse.setDescription(subject.getDescription());

        return courseResponse;

    }



    @Override
    public SubjectResponse updateSubject(Long subjectId, SubjectRequest subjectRequest) {
        Subject subjectDB = subjectRepository.findById(subjectId).
                orElseThrow(()->new RuntimeException("Subject not found"));

        if(Objects.nonNull(subjectRequest.getName())&&!"".
                equalsIgnoreCase(subjectRequest.getName())){
            subjectDB.setName(subjectRequest.getName());
        }

        if (Objects.nonNull(subjectRequest.getDescription())&&!"".
                equalsIgnoreCase(subjectRequest.getDescription())){
            subjectDB.setDescription(subjectRequest.getDescription());
        }
        Subject saved = subjectRepository.save(subjectDB);
        SubjectResponse respo = new SubjectResponse();
        respo.setSubjectId(saved.getSubjectId());
        respo.setName(saved.getName());
        respo.setDescription(saved.getDescription());

        return respo;

    }


}
