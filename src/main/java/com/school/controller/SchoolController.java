package com.school.controller;
import com.school.request.SchoolRequest;
import com.school.response.SchoolResponse;
import com.school.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/schoo/{schoolId}")
    public ResponseEntity<SchoolResponse> getSchoolById(@PathVariable("schoolId")Long schoolId){
        SchoolResponse respo = schoolService.getSchoolById(schoolId);
        return ResponseEntity.ok(respo);
    }

    @GetMapping("/schools")
    public ResponseEntity<List<SchoolResponse>>getAllSchools(){
        List<SchoolResponse> schoolResponses = schoolService.getAllSchools();
        return ResponseEntity.ok(schoolResponses);
    }

    @DeleteMapping("/school/{schoolId}")
    public ResponseEntity<String> deleteById(@PathVariable("SchoolId")Long schoolId){
         schoolService.deleteById(schoolId);
        return ResponseEntity.ok("Deleted successfully");
    }

    @DeleteMapping("/schools")
    public ResponseEntity<String>deleteAllSchools(){
        schoolService.deleteAllSchools();
        return ResponseEntity.ok("All the schools are deleted");
    }

}
