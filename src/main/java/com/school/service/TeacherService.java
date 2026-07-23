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

@Service
@RequiredArgsConstructor
public class TeacherService implements TeacherServe {

 private final TeacherRepo teacherRepo;
 private final UserRepository userRepository;
 private final SchoolClassesRepository schoolClassesRepository;
 private final AssignmentRepository assignmentRepository;
 private final StudentRepository studentRepo;

 @Override
 public TeacherResponse getTeacherByName(String name) {
  throw new UnsupportedOperationException("Not implemented yet");
 }

 @Override
 public TeacherResponse getTeacherByid(Long teacherId) {
  Teacher teacher = teacherRepo.findById(teacherId)
          .orElseThrow(() -> new RuntimeException("Teacher not found!"));
  return mapToDto(teacher);
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
  Teacher teacherDB = teacherRepo.findById(teacherId)
          .orElseThrow(() -> new RuntimeException("Teacher not found"));

  if (Objects.nonNull(teacherRequest.getTeacherNo()) && !teacherRequest.getTeacherNo().isBlank()) {
   teacherDB.setTeacherNo(teacherRequest.getTeacherNo());
  }

  if (Objects.nonNull(teacherRequest.getPhoneNumber()) && !teacherRequest.getPhoneNumber().isBlank()) {
   teacherDB.setPhoneNumber(teacherRequest.getPhoneNumber());
  }

  if (Objects.nonNull(teacherRequest.getClassesId())) {
   SchoolClasses classes = schoolClassesRepository.findById(teacherRequest.getClassesId())
           .orElseThrow(() -> new RuntimeException("Class not found"));
   teacherDB.setClasses(classes);
  }

  if (Objects.nonNull(teacherRequest.getUserId())) {
   UserReg user = userRepository.findById(teacherRequest.getUserId())
           .orElseThrow(() -> new RuntimeException("User not found!"));
   teacherDB.setUserReg(user);
  }

  Teacher saved = teacherRepo.save(teacherDB);
  return mapToDto(saved);
 }

 @Override
 public List<TeacherClassSummary> getMyClasses(Long teacherId) {
  List<Assignment> assignments = assignmentRepository.findByTeacher_TeacherId(teacherId);
  return assignments.stream()
          .map(this::toSummary)
          .toList();
 }

 private TeacherClassSummary toSummary(Assignment assignment) {
  Subject subject = assignment.getSubject();
  SchoolClasses schoolClass = subject.getSchoolClasses();

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
                          .build())
                  .toList())
          .build();
 }

 private TeacherResponse mapToDto(Teacher teacher) {
  TeacherResponse resp = new TeacherResponse();
  resp.setTeacherId(teacher.getTeacherId());
  resp.setTeacherNo(teacher.getTeacherNo());
  resp.setFirstName(teacher.getUserReg().getFirstName());
  resp.setLastName(teacher.getUserReg().getLastName());
  resp.setEmail(teacher.getUserReg().getEmail());
  resp.setPhoneNumber(teacher.getPhoneNumber());
  if (teacher.getClasses() != null) {
   resp.setClassName(teacher.getClasses().getName());
  }
  return resp;
 }
}