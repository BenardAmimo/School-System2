package com.school.controller;

import com.school.request.TermRequest;
import com.school.response.TermResponse;
import com.school.service.TermService;
import org.apache.catalina.LifecycleState;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
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

    @GetMapping("/terms")
    public ResponseEntity <List<TermResponse>> getAllTerms(){
        return ResponseEntity.ok(termService.getAllTerms());
    }

}
