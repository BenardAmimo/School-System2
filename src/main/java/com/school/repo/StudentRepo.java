
package com.school.repo;
import org.springframework.data.jpa.repository.JpaRepository;
import com.school.entity.Student;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepo extends JpaRepository<Student, Long> {
    Student findByNameIgnoreCase(String name);
}
