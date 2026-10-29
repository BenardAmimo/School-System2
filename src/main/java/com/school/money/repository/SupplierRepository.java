// repo/SupplierRepository.java
package com.school.money.repository;


import com.school.money.entity.moneyEntities.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}