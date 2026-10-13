package com.school.repo;

import com.school.entity.Assignment;
import com.school.response.AssignmentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentRepository  extends JpaRepository<Assignment,Long> {
    Optional<Assignment> findByClasses_ClassesIdAndSubject_SubjectId(Long classesId, Long subjectId);
    List<Assignment> findByClasses_ClassesId(Long classesId);
    List<Assignment> findByTeacher_TeacherId(Long teacherId);
    boolean existsByTeacher_TeacherIdAndSubject_SubjectIdAndClasses_ClassesId(Long t, Long s, Long c);
}

