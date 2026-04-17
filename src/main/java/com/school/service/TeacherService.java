
package com.school.service;
import com.school.entity.Course;
import com.school.entity.Teacher;
import com.school.request.TeacherRequest;
import com.school.response.TeacherResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.school.repo.*;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TeacherService implements TeacherServe{
 private final TeacherRepo teacherRepo;

 @Override
 public TeacherResponse createTeacher(TeacherRequest teacherRequest) {

  Teacher teacher = new Teacher();
  teacher.setName(teacherRequest.getName());
  teacher.setEmail(teacherRequest.getEmail());

  Teacher teach = teacherRepo.save(teacher);

  TeacherResponse res = new TeacherResponse();
  res.setTeacherId(teach.getTeacherId());
  res.setName(teach.getName());
  res.setEmail(teach.getEmail());

  return res;
 }

 @Override
 public TeacherResponse getTeacherByName(String name) {
  return null;
 }

 @Override
 public TeacherResponse getTeacherByid(Long teacherId) {

  Teacher teacher = teacherRepo.findById(teacherId).
          orElseThrow(()->new RuntimeException("Teachernot found!"));

  TeacherResponse teacherResponse = new TeacherResponse();

  teacherResponse.setTeacherId(teacher.getTeacherId());
  teacherResponse.setName(teacher.getName());
  teacherResponse.setEmail(teacher.getEmail());

  return teacherResponse;
 }

 @Override
 public List<TeacherResponse> getAllTeachers() {
  return teacherRepo.findAll()
          .stream()
          .map(this::mapToDto)
          .toList();
 }

 @Override
 public TeacherResponse updateTeacher(Long teacherId, TeacherRequest teacherRequest) {
  Teacher teacherDB = teacherRepo.findById(teacherId).
          orElseThrow(()->new RuntimeException("Teacher Not found"));

  if(Objects.nonNull(teacherRequest.getName())&&!"".equalsIgnoreCase(teacherRequest.getName())){
   teacherDB.setName(teacherRequest.getName());
  }

  if(Objects.nonNull(teacherRequest.getEmail())&&!"".equalsIgnoreCase(teacherRequest.getEmail())){
   teacherDB.setEmail(teacherRequest.getEmail());
  }

  Teacher teach = teacherRepo.save(teacherDB);

  TeacherResponse responding = new TeacherResponse();

  responding.setTeacherId(teach.getTeacherId());
  responding.setName(teach.getName());
  responding.setEmail(teach.getEmail());


  return responding;
 }

 private TeacherResponse mapToDto(Teacher teacher) {
  TeacherResponse resp = new TeacherResponse();
  resp.setTeacherId(teacher.getTeacherId());
  resp.setName(teacher.getName());
  resp.setEmail(teacher.getEmail());

  return resp;
 }
}
