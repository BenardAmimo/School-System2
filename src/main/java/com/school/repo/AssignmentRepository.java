package com.school.repo;

import com.school.entity.Assignment;
import com.school.response.AssignmentResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository  extends JpaRepository<Assignment,Long> {
    List<Assignment> findByTeacher_TeacherId(Long teacherId);

}

