package com.school.request;

import com.school.entity.Gender;

public record StudentRequest(
        String firstName,
        String lastName,
        Long age,
        Gender gender,
        Long parentId,
        Long classesId
) {



}
