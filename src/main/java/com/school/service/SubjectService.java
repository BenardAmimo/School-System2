package com.school.service;

import com.school.entity.SchoolClasses;
import com.school.entity.Subject;
import com.school.repo.SchoolClassesRepository;
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
    private final SchoolClassesRepository schoolClassesRepository;

    @Override
    public SubjectResponse createSubject(SubjectRequest subjectRequest) {
        SchoolClasses classes = schoolClassesRepository.findById(subjectRequest.getClassesId())
                .orElseThrow(() -> new RuntimeException("No classes Available"));

        Subject subject = new Subject();
        subject.setName(subjectRequest.getName());
        subject.setSchoolClasses(classes);
        subject.setDescription(subjectRequest.getDescription());

        Subject saved = subjectRepository.save(subject);
        return mapToDto(saved);
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
        res.setClassesName(subject.getSchoolClasses() != null ? subject.getSchoolClasses().getName() : "Unassigned");
        res.setAssignmentCount(subject.getAssignment() != null ? subject.getAssignment().size() : 0);
        return res;
    }

    @Override
    public SubjectResponse getBySubjectName(String name) {
        Subject subject = subjectRepository.findByNameIgnoreCase(name);
        return mapToDto(subject);
    }

    @Override
    public void deleteSubjectById(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject Not found"));
        subjectRepository.delete(subject);
    }

    @Override
    public void deleteAllSubjects() {
        subjectRepository.deleteAll();
    }

    @Override
    public SubjectResponse getByIdSubjectId(Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject Not found"));
        return mapToDto(subject);
    }

    @Override
    public SubjectResponse updateSubject(Long subjectId, SubjectRequest subjectRequest) {
        Subject subjectDB = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        if (Objects.nonNull(subjectRequest.getName()) && !subjectRequest.getName().isBlank()) {
            subjectDB.setName(subjectRequest.getName());
        }

        if (Objects.nonNull(subjectRequest.getDescription()) && !subjectRequest.getDescription().isBlank()) {
            subjectDB.setDescription(subjectRequest.getDescription());
        }

        if (Objects.nonNull(subjectRequest.getClassesId())) {
            SchoolClasses classes = schoolClassesRepository.findById(subjectRequest.getClassesId())
                    .orElseThrow(() -> new RuntimeException("No classes Available"));
            subjectDB.setSchoolClasses(classes);
        }

        Subject saved = subjectRepository.save(subjectDB);
        return mapToDto(saved);
    }
}