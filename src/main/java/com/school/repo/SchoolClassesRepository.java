package com.school.repo;

import com.school.entity.SchoolClasses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolClassesRepository extends JpaRepository<SchoolClasses,Long> {
}
