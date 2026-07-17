package com.school.service;

import com.school.request.SubjectRequest;
import com.school.response.SubjectResponse;

import java.util.List;

public interface SubjectServe {

    SubjectResponse getBySubjectName(String name);

    void deleteSubjectById(Long subjectId);

    void deleteAllSubjects();

    SubjectResponse getByIdSubjectId(Long subjectId);

    SubjectResponse createSubject(SubjectRequest subjectRequest);

    List<SubjectResponse> fetchAllSubjects();

    SubjectResponse updateSubject(Long subjectId, SubjectRequest subjectRequest);
}
