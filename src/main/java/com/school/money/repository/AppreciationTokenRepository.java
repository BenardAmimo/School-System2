// repo/AppreciationTokenRepository.java
package com.school.money.repository;

import com.school.money.entity.TokenStatus;
import com.school.money.entity.moneyEntities.AppreciationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AppreciationTokenRepository extends JpaRepository<AppreciationToken, Long> {
    List<AppreciationToken> findAllByOrderByIdDesc();
    List<AppreciationToken> findByStaffUsernameOrderByIdDesc(String u);
    List<AppreciationToken> findByStaffUsernameAndStatus(String u, TokenStatus s);
    List<AppreciationToken> findByStatus(TokenStatus s);
}