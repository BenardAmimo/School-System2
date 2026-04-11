package com.school.repo;

import com.school.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentRepository  extends JpaRepository<Assignment,Long> {


}

