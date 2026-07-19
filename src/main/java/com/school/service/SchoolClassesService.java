package com.school.service;

import com.school.entity.SchoolClasses;
import com.school.repo.SchoolClassesRepository;
import com.school.request.SchoolClassesRequest;
import com.school.response.SchoolClassesResponse;
import org.springframework.stereotype.Service;

@Service
public class SchoolClassesService implements SchoolClassesServ{
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
}
