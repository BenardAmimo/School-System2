package com.school.service;

import com.school.request.SchoolRequest;
import com.school.response.SchoolResponse;

import java.util.List;

public interface SchoolServInter {
    SchoolResponse createNewSchool(SchoolRequest schoolRequest);

    SchoolResponse getSchoolById( Long schoolId);

    List<SchoolResponse> getAllSchools();

    void deleteById(Long schoolId);

    void deleteAllSchools();
}
