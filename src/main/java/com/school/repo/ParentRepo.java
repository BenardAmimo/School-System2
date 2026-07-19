
package com.school.repo;
import org.springframework.data.jpa.repository.JpaRepository;
import com.school.entity.Parent;
import org.springframework.stereotype.Repository;

@Repository
public interface ParentRepo extends JpaRepository<Parent, Long> {

}
