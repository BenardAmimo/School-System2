package com.school.service;

import com.school.entity.Assignment;
import com.school.entity.SchoolClasses;
import com.school.entity.Subject;
import com.school.entity.Teacher;
import com.school.repo.SchoolClassesRepository;
import com.school.request.SchoolClassesRequest;
import com.school.response.ClassStudentSummary;
import com.school.response.ClassSubjectSummary;
import com.school.response.ClassTeacherSummary;
import com.school.response.SchoolClassesResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class SchoolClassesService implements SchoolClassesServ {
    private final SchoolClassesRepository classesRepository;

    public SchoolClassesService(SchoolClassesRepository classesRepository) {
        this.classesRepository = classesRepository;
    }

    @Transactional
    @Override
    public SchoolClassesResponse createClasses(SchoolClassesRequest schoolClassesRequest) {
        SchoolClasses classes = new SchoolClasses();

        classes.setName(schoolClassesRequest.name());
        classes.setYear(schoolClassesRequest.year());
        classes.setLocation(schoolClassesRequest.location());

        SchoolClasses saved = classesRepository.save(classes);
        return tomapping(saved);
    }

    // readOnly transaction: tomapping() walks lazy collections (students, assignments),
    // which only works while the session is open.
    @Transactional(readOnly = true)
    @Override
    public List<SchoolClassesResponse> getAllClasses() {
        return classesRepository
                .findAll()
                .stream()
                .map(this::tomapping)
                .toList();
    }

    @Transactional
    @Override
    public void deleteClasses(Long classesId) {
        SchoolClasses classes = classesRepository.findById(classesId)
                .orElseThrow(() -> new IllegalArgumentException("Class not in the System"));
        classesRepository.delete(classes);
    }

    @Transactional
    @Override
    public SchoolClassesResponse updateClasses(Long classesId, SchoolClassesRequest classesRequest) {
        SchoolClasses classDB = classesRepository.findById(classesId)
                .orElseThrow(() -> new IllegalArgumentException("Classes not found in the System"));

        if (Objects.nonNull(classesRequest.location()) && !classesRequest.location().isBlank()) {
            classDB.setLocation(classesRequest.location());
        }

        if (Objects.nonNull(classesRequest.name()) && !classesRequest.name().isBlank()) {
            classDB.setName(classesRequest.name());
        }

        if (Objects.nonNull(classesRequest.year()) && !classesRequest.year().isBlank()) {
            classDB.setYear(classesRequest.year());
        }

        SchoolClasses saving = classesRepository.save(classDB);
        return tomapping(saving);
    }

    private SchoolClassesResponse tomapping(SchoolClasses classes) {

        SchoolClassesResponse response = new SchoolClassesResponse();
        response.setName(classes.getName());
        response.setClassesId(classes.getClassesId());
        response.setYear(classes.getYear());
        response.setLocation(classes.getLocation());
        response.setStudentsCount(classes.getStudents() != null ? classes.getStudents().size() : 0);

        response.setStudent(
                classes.getStudents() == null ? Collections.emptyList() :
                        classes.getStudents().stream()
                                .map(s -> new ClassStudentSummary(
                                        s.getStudentId(), s.getFirstName(), s.getLastName(), s.getGender(), s.getAge()))
                                .toList()
        );

        // Subjects and teachers both come staffing this class's assignments, so a teacher
        // shows up as soon as a subject is assigned, with no timetable slot needed.
        Collection<Assignment> assignments =
                classes.getAssignments() == null ? List.of() : classes.getAssignments();

        // De-duplicate by id, never with distinct(): distinct() calls equals/hashCode on entities,
        // which can loop forever when entities reference each other.
        Map<Long, Subject> subjects = new LinkedHashMap<>();
        Map<Long, Teacher> teachers = new LinkedHashMap<>();
        for (Assignment assignment : assignments) {
            Subject subject = assignment.getSubject();
            if (subject != null) {
                subjects.putIfAbsent(subject.getSubjectId(), subject);
            }
            Teacher teacher = assignment.getTeacher();
            if (teacher != null) {
                teachers.putIfAbsent(teacher.getTeacherId(), teacher);
            }
        }

        response.setSubjects(
                subjects.values().stream()
                        .map(sub -> new ClassSubjectSummary(sub.getSubjectId(), sub.getName()))
                        .toList()
        );

        response.setTeachers(
                teachers.values().stream()
                        .map(t -> new ClassTeacherSummary(
                                t.getTeacherId(),
                                t.getUserReg() != null ? t.getUserReg().getFirstName() : null,
                                t.getUserReg() != null ? t.getUserReg().getLastName() : null,
                                t.getGender(),
                                t.getPhoneNumber(),
                                t.getAge()))
                        .toList()
        );

        return response;
    }
}