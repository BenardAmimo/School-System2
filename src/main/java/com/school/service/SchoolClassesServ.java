package com.school.service;

import com.school.request.SchoolClassesRequest;
import com.school.response.SchoolClassesResponse;

public interface SchoolClassesServ {
    SchoolClassesResponse createClasses(SchoolClassesRequest schoolClassesRequest);
}
