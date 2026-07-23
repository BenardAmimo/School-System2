package com.school.service;

import com.school.request.SchoolClassesRequest;
import com.school.response.SchoolClassesResponse;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

public interface SchoolClassesServ {
    SchoolClassesResponse createClasses(SchoolClassesRequest schoolClassesRequest);

    List<SchoolClassesResponse> getAllClasses();
}
