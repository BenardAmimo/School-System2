
package com.school.controller;
import com.school.entity.Teacher;
import com.school.repo.TeacherRepo;
import com.school.response.TeacherClassSummary;
import com.school.response.TeacherResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.school.service.TeacherService;
import com.school.request.TeacherRequest;

import java.util.List;

@RestController
public class TeacherController {

 private final TeacherService teacherservice;
 private final TeacherRepo teacherRepo;

 public TeacherController(TeacherService s, TeacherRepo teacherRepo){this.teacherservice=s;
     this.teacherRepo = teacherRepo;
 }

 @GetMapping("/teacher/{name}")
 public ResponseEntity<TeacherResponse> getTeacherByName(@PathVariable("name")String name){
     return ResponseEntity.ok(teacherservice.getTeacherByName(name));
 }

 @GetMapping("/teacher/id/{teacherId}")
 public ResponseEntity<TeacherResponse>getTeacherById(@PathVariable("teacherId") Long teacherId){
     TeacherResponse teach =  teacherservice.getTeacherByid(teacherId);
     return ResponseEntity.ok(teach);
 }
 @GetMapping("/teachers")
 public ResponseEntity<List<TeacherResponse>> getAllTeachers(){
     List<TeacherResponse> teachers = teacherservice.getAllTeachers();
     return ResponseEntity.ok(teachers);
 }

 @PutMapping("/teacher/id/{teacherId}")
    public ResponseEntity<TeacherResponse> updateTeachers(@PathVariable("teacherId")Long teacherId
         ,@RequestBody TeacherRequest teacherRequest){
     TeacherResponse repond = teacherservice.updateTeacher(teacherId,teacherRequest);
     return ResponseEntity.status(202).body(repond);

 }

    @GetMapping("/me/classes")
    @PreAuthorize("hasAnyRole('TEACHER')")
    public ResponseEntity<List<TeacherClassSummary>> getMyClasses(Authentication authentication) {
        String email = authentication.getName(); // UserReg.getUsername() returns email

        Teacher teacher = teacherRepo.findByUserReg_Email(email)
                .orElseThrow(() -> new RuntimeException("This account is not linked to a teacher record"));

        return ResponseEntity.ok(teacherservice.getMyClasses(teacher.getTeacherId()));
    }

    @DeleteMapping("/teacher/{teacherId}")
    public ResponseEntity<String> deleteTeacher(@PathVariable("teacherId") Long teacherId){
       teacherservice.deleteTeacher(teacherId);
       return ResponseEntity.ok("Teacher deleted!");
    }
 

}
