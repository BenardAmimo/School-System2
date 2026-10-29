// repo/StaffPayProfileRepository.java
package com.school.money.repository;

import com.school.money.entity.moneyEntities.StaffPayProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StaffPayProfileRepository extends JpaRepository<StaffPayProfile, Long> {
    Optional<StaffPayProfile> findFirstByStaffUsernameAndEffectiveFromLessThanEqualOrderByEffectiveFromDescIdDesc(String u, LocalDate d);
    List<StaffPayProfile> findByStaffUsernameOrderByEffectiveFromDesc(String u);

    @Query("select distinct p.staffUsername from StaffPayProfile p")
    List<String> findAllStaffUsernames();
}