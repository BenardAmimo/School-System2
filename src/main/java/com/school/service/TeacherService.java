package com.school.service;

import com.school.entity.Assignment;
import com.school.entity.SchoolClasses;
import com.school.entity.Student;
import com.school.entity.Subject;
import com.school.entity.Teacher;
import com.school.request.TeacherRequest;
import com.school.response.StudentSummary;
import com.school.response.TeacherClassSummary;
import com.school.response.TeacherResponse;
import com.school.security.entity.UserReg;
import com.school.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.school.repo.*;

import java.util.List;
import java.util.Objects;

import static com.school.response.TeacherResponse.from;

@Service
@RequiredArgsConstructor
public class TeacherService implements TeacherServe {

 private final TeacherRepo teacherRepo;
 private final AssignmentRepository assignmentRepository;
 private final StudentRepository studentRepo;
 private final UserRepository userRepository;

 @Override
 public TeacherResponse getTeacherByName(String name) {
  throw new UnsupportedOperationException("Not implemented yet");
 }

 @Override
 public TeacherResponse getTeacherByid(Long teacherId) {
  Teacher teacher = teacherRepo.findById(teacherId)
          .orElseThrow(() -> new RuntimeException("Teacher not found!"));
  return from(teacher);
 }

 @Override
 public List<TeacherResponse> getAllTeachers() {
  return teacherRepo.findAll()
          .stream()
          .map(TeacherResponse::from)
          .toList();
 }

 @Override
 public TeacherResponse updateTeacher(Long teacherId, TeacherRequest teacherRequest) {
  Teacher teacherDB = teacherRepo.findById(teacherId)
          .orElseThrow(() -> new RuntimeException("Teacher not found"));

  if (Objects.nonNull(teacherRequest.teacherNo()) && !teacherRequest.teacherNo().isBlank()) {
   teacherDB.setTeacherNo(teacherRequest.teacherNo());
  }

  if (Objects.nonNull(teacherRequest.phoneNumber()) && !teacherRequest.phoneNumber().isBlank()) {
   teacherDB.setPhoneNumber(teacherRequest.phoneNumber());
  }
  if (Objects.nonNull(teacherRequest.age())){
   teacherDB.setAge(teacherRequest.age());
  }

  if (Objects.nonNull(teacherRequest.userId())) {
   UserReg user = userRepository.findById(teacherRequest.userId())
           .orElseThrow(() -> new RuntimeException("User not found!"));
   teacherDB.setUserReg(user);
  }

  Teacher saved = teacherRepo.save(teacherDB);
  return from(saved);
 }

 @Override
 public List<TeacherClassSummary> getMyClasses(Long teacherId) {
  List<Assignment> assignments = assignmentRepository.findByTeacher_TeacherId(teacherId);
  return assignments.stream()
          .map(this::toSummary)
          .toList();
 }

 @Override
 public void deleteTeacher(Long teacherId) {
  Teacher teach = teacherRepo.findById(teacherId)
          .orElseThrow(()->new RuntimeException("Teacher not found!"));

   teacherRepo.delete(teach);
 }
//issue
 private TeacherClassSummary toSummary(Assignment assignment) {
  Subject subject = assignment.getSubject();
  SchoolClasses schoolClass = assignment.getClasses();

  List<Student> students = schoolClass == null
          ? List.of()
          : studentRepo.findByClasses_ClassesId(schoolClass.getClassesId());

  return TeacherClassSummary.builder()
          .assignmentId(assignment.getAssignmentId())
          .subjectName(subject.getName())
          .className(schoolClass != null ? schoolClass.getName() : "Unassigned")
          .classLocation(schoolClass != null ? schoolClass.getLocation() : null)
          .students(students.stream()
                  .map(s -> StudentSummary.builder()
                          .studentId(s.getStudentId())
                          .firstName(s.getFirstName())
                          .lastName(s.getLastName())
                          .gender(s.getGender())
                          .age(s.getAge())
                          .build())
                  .toList())
          .build();
 }

}