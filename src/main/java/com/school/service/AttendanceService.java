package com.school.service;

import com.school.entity.Attendance;
import com.school.entity.SchoolClasses;

import com.school.repo.AttendanceRepository;
import com.school.repo.SchoolClassesRepository;
import com.school.repo.StudentRepository;
import com.school.repo.SubjectRepository;
import com.school.request.AttendanceRequest;
import com.school.response.AttendanceResponse;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AttendanceService implements AttendanceServing {
    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepo;
    private final SubjectRepository subjectRepository;
    private final SchoolClassesRepository classesRepository;

    public AttendanceService(AttendanceRepository attendanceRepository, StudentRepository studentRepo, SubjectRepository subjectRepository, SchoolClassesRepository classesRepository) {
        this.attendanceRepository = attendanceRepository;
        this.studentRepo = studentRepo;
        this.subjectRepository = subjectRepository;
        this.classesRepository = classesRepository;
    }

    @Override
    public AttendanceResponse markAttendance(AttendanceRequest request) {

    // Student students = studentRepo.findById(request.getStudentId()).orElseThrow(()->new RuntimeException("Student Not Found"));
     //   Subject subjects = subjectRepository.findById(request.getSubjectId()).orElseThrow(()->new RuntimeException("No subject Found"));

        SchoolClasses classes = classesRepository.findById(request.getClassId())
                .orElseThrow(()->new RuntimeException("No Class found"));


        Attendance attend = new Attendance();
        attend.setAttendingTime(request.getAttendingTime());
        attend.setCheckoutTime(request.getCheckoutTime());
      //  attend.setStudent((List<Student>) students);
      //  attend.setSubjects(subjects);
        attend.setClasses(classes);

        Attendance saving = attendanceRepository.save(attend);

        AttendanceResponse response = AttendanceResponse
                .builder()
                .attendanceId(saving.getAttendanceId())
                .attendingTime(saving.getAttendingTime())
                .checkoutTime(saving.getCheckoutTime())
                .className(saving.getClasses().getName())
                .build();


        return response;
    }

    @Override
    public List<AttendanceResponse> getAllAttendance() {
        return attendanceRepository
                .findAll()
                .stream()
                .map(this::toMapping)
                .toList();
    }

    private AttendanceResponse toMapping(Attendance attendance){

        AttendanceResponse attending = AttendanceResponse
                .builder()
                .attendanceId(attendance.getAttendanceId())
                .attendingTime(attendance.getAttendingTime())
                .checkoutTime(attendance.getCheckoutTime())
                //.subject(attendance.getSubjects().getName())
                .className(attendance.getClasses().getName())
                .build();

        return attending;

    }
}
