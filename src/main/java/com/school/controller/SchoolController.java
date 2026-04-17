package com.school.controller;

import com.school.request.SchoolRequest;
import com.school.response.SchoolResponse;
import com.school.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SchoolController {
    private final SchoolService schoolService;
    @PostMapping("/school")
    public ResponseEntity<SchoolResponse> createNewSchool(@RequestBody SchoolRequest schoolRequest){
        SchoolResponse res = schoolService.createNewSchool(schoolRequest);
        return ResponseEntity.status(202).body(res);
    }
}
