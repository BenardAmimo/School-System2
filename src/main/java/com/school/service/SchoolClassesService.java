package com.school.service;

import com.school.entity.SchoolClasses;
import com.school.repo.SchoolClassesRepository;
import com.school.request.SchoolClassesRequest;
import com.school.response.ClassStudentSummary;
import com.school.response.ClassSubjectSummary;
import com.school.response.ClassTeacherSummary;
import com.school.response.SchoolClassesResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SchoolClassesService implements SchoolClassesServ {
    private final SchoolClassesRepository classesRepository;

    public SchoolClassesService(SchoolClassesRepository classesRepository) {
        this.classesRepository = classesRepository;
    }

    @Override
    public SchoolClassesResponse createClasses(SchoolClassesRequest schoolClassesRequest) {
        SchoolClasses classes = new SchoolClasses();

        classes.setName(schoolClassesRequest.getName());
        classes.setYear(schoolClassesRequest.getYear());
        classes.setLocation(schoolClassesRequest.getLocation());

        SchoolClasses saved = classesRepository.save(classes);

        SchoolClassesResponse respo = new SchoolClassesResponse();
        respo.setClassesId(saved.getClassesId());
        respo.setName(saved.getName());
        respo.setLocation(saved.getLocation());
        return respo;
    }

    @Override
    public List<SchoolClassesResponse> getAllClasses() {
        return classesRepository
                .findAll()
                .stream()
                .map(this::tomapping)
                .toList();
    }

    private SchoolClassesResponse tomapping(SchoolClasses classes) {
        SchoolClassesResponse response = new SchoolClassesResponse();
        response.setName(classes.getName());
        response.setClassesId(classes.getClassesId());
        response.setYear(classes.getYear());
        response.setLocation(classes.getLocation());
        response.setStudentsCount(classes.getStudent() != null ? classes.getStudent().size() : 0);

        response.setStudent(
                classes.getStudent() == null ? Collections.emptyList() :
                        classes.getStudent().stream()
                                .map(s -> new ClassStudentSummary(
                                        s.getStudentId(), s.getFirstName(), s.getLastName()))
                                .toList()
        );

        response.setSubjects(
                classes.getSubjects() == null ? Collections.emptyList() :
                        classes.getSubjects().stream()
                                .map(sub -> new ClassSubjectSummary(
                                        sub.getSubjectId(), sub.getName()))
                                .toList()
        );

        response.setTeachers(
                classes.getTeachers() == null ? Collections.emptyList() :
                        classes.getTeachers().stream()
                                .map(t -> new ClassTeacherSummary(
                                        t.getTeacherId(),
                                        t.getUserReg() != null ? t.getUserReg().getFirstName() : null,
                                        t.getUserReg() != null ? t.getUserReg().getLastName() : null))
                                .toList()
        );

        return response;
    }
}