
package com.school.repo;
import org.springframework.data.jpa.repository.JpaRepository;
import com.school.entity.Subject;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Subject findByNameIgnoreCase(String name);

}
