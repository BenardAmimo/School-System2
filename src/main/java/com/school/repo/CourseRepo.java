
package com.school.repo;
import org.springframework.data.jpa.repository.JpaRepository;
import com.school.entity.Course;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepo extends JpaRepository<Course, Long> {
    Course findByNameIgnoreCase(String name);

}
