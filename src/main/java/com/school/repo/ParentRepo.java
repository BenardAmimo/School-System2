
package com.school.repo;
import org.springframework.data.jpa.repository.JpaRepository;
import com.school.entity.Parent;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentRepo extends JpaRepository<Parent, Long> {
    Optional<Parent> findByUserReg_Email(String email);
}

