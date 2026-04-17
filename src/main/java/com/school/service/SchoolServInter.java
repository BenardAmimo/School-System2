package com.school.service;

import com.school.request.SchoolRequest;
import com.school.response.SchoolResponse;

public interface SchoolServInter {
    SchoolResponse createNewSchool(SchoolRequest schoolRequest);
}
