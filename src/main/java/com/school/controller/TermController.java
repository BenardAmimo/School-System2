package com.school.controller;

import com.school.request.TermRequest;
import com.school.response.TermResponse;
import com.school.service.TermService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TermController {
    private final TermService termService;

    public TermController(TermService termService) {
        this.termService = termService;
    }

    @PostMapping("/term")
    public ResponseEntity<TermResponse> createNewTerm(@RequestBody TermRequest termRequest){
        TermResponse response = termService.createNewTerm(termRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
