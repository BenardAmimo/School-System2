package com.school.controller;

import com.school.request.SchoolClassesRequest;
import com.school.response.SchoolClassesResponse;
import com.school.service.SchoolClassesService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SchoolClassesController {
    private final SchoolClassesService schoolClassesService;

    public SchoolClassesController(SchoolClassesService schoolClassesService) {
        this.schoolClassesService = schoolClassesService;
    }

    @PostMapping("/classes")
    public ResponseEntity<SchoolClassesResponse> createClasses(@RequestBody SchoolClassesRequest schoolClassesRequest){
        SchoolClassesResponse response = schoolClassesService.createClasses(schoolClassesRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
