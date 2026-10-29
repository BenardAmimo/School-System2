// repo/PayeeAccountRepository.java
package com.school.money.repository;

import com.school.money.entity.PayeeType;
import com.school.money.entity.moneyEntities.PayeeAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PayeeAccountRepository extends JpaRepository<PayeeAccount, Long> {
    Optional<PayeeAccount> findByPayeeTypeAndPayeeRef(PayeeType type, String ref);
}