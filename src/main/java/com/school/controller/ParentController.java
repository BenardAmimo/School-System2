package com.school.controller;

import com.school.request.ParentRequest;
import com.school.response.ParentResponse;
import com.school.service.ParentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@Slf4j
public class ParentController {
    private final ParentService parentService;

    public ParentController(ParentService parentService) {
        this.parentService = parentService;
    }


    @GetMapping("/parents")
    public ResponseEntity <List<ParentResponse>> getAllParents(){
        log.info("Get mapping of all parents");
        List<ParentResponse> parents = parentService.getAllParents();

        return ResponseEntity.ok(parents);
    }

    @GetMapping("/parent/id/{parentId}")
    public ResponseEntity<ParentResponse> getParentById(@PathVariable("parentId") Long parentId){
        log.info("Get request for parent with id {} ",parentId);
        ParentResponse parent = parentService.getParentById(parentId);
        log.info("Parent found with id {} ",parent.getParentId());
        return ResponseEntity.ok(parent);
    }

    @PutMapping("/parent/update/{parentId}")
    public ResponseEntity<ParentResponse> updateParent(@PathVariable("parentId")Long parentId,
                                                        @RequestBody ParentRequest parentRequest){
        ParentResponse updating = parentService.updateParent(parentId,parentRequest);

        return ResponseEntity.status(202).body(updating);
    }


    @GetMapping("/parent/name/{name}")
    public ResponseEntity<ParentResponse>getParentByName(@PathVariable("name")String name){
        log.info("Get Request for parent: {} ",name);
        ParentResponse parentResponse = parentService.getParentByName(name);
        log.info("Parent with id {} found",parentResponse.getParentId());
        return ResponseEntity.ok(parentResponse);
    }


    @DeleteMapping("/parent/id/{parentId}")
    public ResponseEntity<String> deleteParent(@PathVariable("parentId")Long parentId){
        log.info("Delete Request for parent id {} ",parentId);
            ParentResponse parent = parentService.deleteParent(parentId);
            log.info("parent {} successfully deleted",parent.getParentId());
        return ResponseEntity.ok("Successfully deleted");
    }
}
