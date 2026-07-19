package com.school.service;

import com.school.request.ParentRequest;
import com.school.response.ParentResponse;

import java.util.List;
public interface ParentServe {
    List<ParentResponse> getAllParents();

    ParentResponse getParentById(Long parentId);

    ParentResponse updateParent(Long parentId, ParentRequest parentRequest);

    ParentResponse getParentByName(String name);

    ParentResponse deleteParent(Long parentId);
}
