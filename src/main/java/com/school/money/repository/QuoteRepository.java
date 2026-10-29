
package com.school.money.repository;

import com.school.money.entity.moneyEntities.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
    List<Quote> findByExpenseIdOrderByAmountAsc(Long expenseId);
    long countByExpenseId(Long expenseId);
}