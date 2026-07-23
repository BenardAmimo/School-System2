package com.school.repo;

import com.school.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student,Long> {
    List<Student> findByClasses_ClassesId(Long classesId);
    List<Student> findByParent_ParentId(Long parentId);
}
