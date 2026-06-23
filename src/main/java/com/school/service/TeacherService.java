
package com.school.service;
import com.school.entity.Course;
import com.school.entity.Teacher;
import com.school.request.TeacherRequest;
import com.school.response.TeacherResponse;
import com.school.security.entity.UserReg;
import com.school.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.school.repo.*;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TeacherService implements TeacherServe{
 private final TeacherRepo teacherRepo;
 private final UserRepository userRepository;


 @Override
 public TeacherResponse getTeacherByName(String name) {
  return null;
 }

 @Override
 public TeacherResponse getTeacherByid(Long teacherId) {

  Teacher teacher = teacherRepo.findById(teacherId).
          orElseThrow(()->new RuntimeException("Teacher not found!"));

  TeacherResponse teacherResponse = new TeacherResponse();

  teacherResponse.setTeacherId(teacher.getTeacherId());
  teacherResponse.setFirstName(teacher.getUserReg().getFirstName());
  teacherResponse.setLastName(teacher.getUserReg().getLastName());
  teacherResponse.setEmail(teacher.getUserReg().getEmail());

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
  UserReg user  = userRepository.findById(teacherRequest.getUserId()).
          orElseThrow(()->new RuntimeException("User not found!"));

  if(Objects.nonNull(teacherRequest.getTeacherNo())&&!"".equalsIgnoreCase(teacherRequest.getTeacherNo())){
   teacherDB.setTeacherNo(teacherRequest.getTeacherNo());
  }

  /*if(Objects.nonNull(teacherRequest.getEmail())&&!"".equalsIgnoreCase(teacherRequest.getEmail())){
   teacherDB.setEmail(teacherRequest.getEmail());
  }*/
  teacherDB.setUserReg(user);
  Teacher teach = teacherRepo.save(teacherDB);

  TeacherResponse responding = new TeacherResponse();

  responding.setTeacherId(teach.getTeacherId());
  responding.setFirstName(teach.getUserReg().getFirstName());
  responding.setLastName(teach.getUserReg().getLastName());
  responding.setEmail(teach.getUserReg().getEmail());


  return responding;
 }

 private TeacherResponse mapToDto(Teacher teacher) {
  TeacherResponse resp = new TeacherResponse();
  resp.setTeacherId(teacher.getTeacherId());
  resp.setFirstName(teacher.getUserReg().getFirstName());
  resp.setLastName(teacher.getUserReg().getLastName());
  resp.setEmail(teacher.getUserReg().getEmail());

  return resp;
 }
}
