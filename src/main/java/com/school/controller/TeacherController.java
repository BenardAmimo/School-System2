
package com.school.controller;
import com.school.entity.Teacher;
import com.school.response.TeacherResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.school.service.TeacherService;
import com.school.request.TeacherRequest;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TeacherController {

 private final TeacherService service;

 public TeacherController(TeacherService s){this.service=s;}

@PostMapping("/teachers")
public ResponseEntity<TeacherResponse> createTeacher(@RequestBody TeacherRequest teacherRequest){
  return ResponseEntity.status(201).body(service.createTeacher(teacherRequest));
 }

 @GetMapping("/{name}")
 public ResponseEntity<TeacherResponse> getTeacherByName(@PathVariable("name")String name){
     return ResponseEntity.ok(service.getTeacherByName(name));
 }

 @GetMapping("/teacher/id/{teacherId}")
 public ResponseEntity<TeacherResponse>getTeacherById(@PathVariable("teacherId") Long teacherId){
     TeacherResponse teach =  service.getTeacherByid(teacherId);
     return ResponseEntity.ok(teach);
 }
 @GetMapping("/teachers")
 public ResponseEntity<List<TeacherResponse>> getAllTeachers(){
     List<TeacherResponse> teachers = service.getAllTeachers();
     return ResponseEntity.ok(teachers);
 }

 @PutMapping("/teacher/id/{teacherId}")
    public ResponseEntity<TeacherResponse> updateTeachers(@PathVariable("teacherId")Long teacherId
         ,@RequestBody TeacherRequest teacherRequest){
     TeacherResponse repond = service.updateTeacher(teacherId,teacherRequest);
     return ResponseEntity.status(202).body(repond);

 }
 

}
