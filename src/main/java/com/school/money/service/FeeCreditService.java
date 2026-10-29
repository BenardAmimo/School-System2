package com.school.money.service;

import com.school.money.Access;
import com.school.money.repository.FeeCreditRepository;
import com.school.money.response.CarriedResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/** Carried-forward money. The fee module must subtract availableFor(...) from what a student owes in the term. */
@Service
public class FeeCreditService {
    private final FeeCreditRepository repo;
    public FeeCreditService(FeeCreditRepository repo) { this.repo = repo; }

    @Transactional(readOnly = true)
    public BigDecimal availableFor(Long studentId, Long termId) {
        BigDecimal v = repo.sumFor(studentId, termId);
        return v == null ? BigDecimal.ZERO : v;
    }

    /** For the fee module: money carried into this term for this student. */
    @Transactional(readOnly = true)
    public CarriedResponse carried(Long studentId, Long termId, Authentication auth) {
        Access.user(auth);
        return new CarriedResponse(availableFor(studentId, termId));
    }
}