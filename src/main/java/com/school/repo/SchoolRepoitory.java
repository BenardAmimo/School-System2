package com.school.repo;

import com.school.entity.School;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchoolRepoitory extends JpaRepository<School,Long> {
}
