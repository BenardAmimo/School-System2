// repo/ExpenseRepository.java
package com.school.money.repository;

import com.school.money.entity.moneyEntities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findAllByOrderByIdDesc();
}