package com.school.money.service;

import com.school.money.Access;
import com.school.money.MoneyException;
import com.school.money.entity.moneyEntities.Supplier;
import com.school.money.repository.SupplierRepository;
import com.school.money.request.SupplierRequest;
import com.school.money.response.SupplierResponse;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierService {
    private final SupplierRepository repo;
    public SupplierService(SupplierRepository repo) { this.repo = repo; }

    // ---- Entry points for the controller ----

    @Transactional(readOnly = true)
    public List<SupplierResponse> list(Authentication auth) {
        Access.preparer(auth);
        return SupplierResponse.list(list());
    }

    @Transactional
    public SupplierResponse create(SupplierRequest r, Authentication auth) {
        Access.preparer(auth);
        return SupplierResponse.from(save(null, toEntity(r)));
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest r, Authentication auth) {
        Access.preparer(auth);
        return SupplierResponse.from(save(id, toEntity(r)));
    }

    // ---- The work itself ----

    @Transactional(readOnly = true)
    public List<Supplier> list() { return repo.findAll(); }

    @Transactional
    public Supplier save(Long id, Supplier in) {
        if (in.getName() == null || in.getName().isBlank()) throw MoneyException.bad("Supplier name is required.");
        Supplier s = id == null ? new Supplier() : repo.findById(id).orElseThrow(() -> MoneyException.notFound("Supplier not found."));
        s.setName(in.getName().trim());
        s.setContactPerson(in.getContactPerson());
        s.setPhone(in.getPhone());
        s.setEmail(in.getEmail());
        s.setKraPin(in.getKraPin());
        s.setActive(in.isActive());
        try {
            return repo.saveAndFlush(s);
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw MoneyException.conflict("A supplier with this name already exists.");
        }
    }

    private static Supplier toEntity(SupplierRequest r) {
        Supplier s = new Supplier();
        s.setName(r.name()); s.setContactPerson(r.contactPerson()); s.setPhone(r.phone());
        s.setEmail(r.email()); s.setKraPin(r.kraPin());
        s.setActive(r.active() == null || r.active());
        return s;
    }
}