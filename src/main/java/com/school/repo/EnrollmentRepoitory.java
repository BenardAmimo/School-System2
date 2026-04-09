package com.school.repo;

import com.school.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface EnrollmentRepoitory extends JpaRepository<Enrollment,Long> {
}
