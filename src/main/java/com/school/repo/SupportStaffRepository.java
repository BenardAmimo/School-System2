package com.school.repo;

import com.school.entity.SupportStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupportStaffRepository extends JpaRepository<SupportStaff,Long> {
}
