
package com.school.repo;
import org.springframework.data.jpa.repository.JpaRepository;
import com.school.entity.Teacher;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepo extends JpaRepository<Teacher, Long> {

}
