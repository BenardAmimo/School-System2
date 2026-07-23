package com.school.controller;

import com.school.error.SubjectNotFoundException;
import com.school.request.SubjectRequest;
import com.school.response.SubjectResponse;
import com.school.service.SubjectService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@Slf4j
public class SubjectController {
    private final SubjectService subjectService;

    public SubjectController(SubjectService subjectService) {
        this.subjectService = subjectService;
    }

    @PostMapping("/subjects")
    public ResponseEntity<SubjectResponse> createCourse(@RequestBody SubjectRequest subjectRequest){
        log.info("POST request to save Subject: {}", subjectRequest);

        SubjectResponse fun = subjectService.createSubject(subjectRequest);

        log.info("Subject saved with id{}",fun.getSubjectId());

        return ResponseEntity.status(201).body(fun);
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<SubjectResponse>> fetchAllSubjects(){
        log.info("Get request for subjects");

        List<SubjectResponse> subjects = subjectService.fetchAllSubjects();

        if (subjects == null) {
            subjects = new ArrayList<>(); //  prevent null
        }

        log.info("Found {} subjects",subjects.size());

        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/subject/id/{subjectId}")
    public ResponseEntity<SubjectResponse> getByIdSubject(@PathVariable("courseId")Long SubjectId) throws SubjectNotFoundException {

        SubjectResponse gett = subjectService.getByIdSubjectId(SubjectId);

        log.info("subject found with id {} ",gett.getSubjectId());
        return ResponseEntity.ok(gett);
    }

    // In CourseController


    @PutMapping("/subject/{subjectId}")
    public ResponseEntity<SubjectResponse> updateSubject(@PathVariable("courseId")Long subjectId ,
                                                         @RequestBody com.school.request.SubjectRequest subjectRequest){
        log.info("Updating subjectName {} ",subjectRequest.getName());

        SubjectResponse updating = subjectService.updateSubject(subjectId,subjectRequest);

        log.info("Subject updated with id {} ",updating.getSubjectId());
        return ResponseEntity.status(202).body(updating);

    }

    @GetMapping("/subject/{name}")
    public ResponseEntity<SubjectResponse> getBySubjectName(@PathVariable("name")String name){

        log.info("Get request with subjectName {} ",name);

        SubjectResponse fun = subjectService.getBySubjectName(name);

        log.info("Subject Found with id {} ",fun.getSubjectId());
        return ResponseEntity.ok(fun);
    }

    @DeleteMapping("/subject/{subjectId}")
    public ResponseEntity<String>deleteSubjectById(@PathVariable("SubjectId")Long subjectId){
        log.info("Delete request for subject with id {} ",subjectId);

        subjectService.deleteSubjectById(subjectId);

        return ResponseEntity.ok("Successfully deleted");
    }

    @DeleteMapping("/deleteAllSubjects")
    public ResponseEntity<String>deleteAllSubjects(){
        log.info("Delete request for all subjects");
         subjectService.deleteAllSubjects();
        return ResponseEntity.ok("All Subjects Have been deleted");
    }

}
