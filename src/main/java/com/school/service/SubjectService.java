package com.school.service;

import com.school.entity.Subject;
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

    @Override
    public SubjectResponse createSubject(SubjectRequest subjectRequest) {

        Subject subject = new Subject();
        subject.setName(subjectRequest.name());
        subject.setDescription(subjectRequest.description());

        Subject saved = subjectRepository.save(subject);
        return SubjectResponse.from(saved);
    }

    @Override
    public List<SubjectResponse> fetchAllSubjects() {
        return subjectRepository.findAll()
                .stream()
                .map(SubjectResponse::from)
                .toList();
    }

    @Override
    public SubjectResponse getBySubjectName(String name) {
        Subject subject = subjectRepository.findByNameIgnoreCase(name);
        return SubjectResponse.from(subject);
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
        return SubjectResponse.from(subject);
    }

    @Override
    public SubjectResponse updateSubject(Long subjectId, SubjectRequest subjectRequest) {
        Subject subjectDB = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new RuntimeException("Subject not found"));

        if (Objects.nonNull(subjectRequest.name()) && !subjectRequest.name().isBlank()) {
            subjectDB.setName(subjectRequest.name());
        }

        if (Objects.nonNull(subjectRequest.description()) && !subjectRequest.description().isBlank()) {
            subjectDB.setDescription(subjectRequest.description());
        }


        Subject saved = subjectRepository.save(subjectDB);
        return SubjectResponse.from(saved);
    }
}