package com.school.money.port;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Uses the entity NAME "UserReg" in JPQL so this module has no compile-time dependency on your security package.
 * UserReg: email (login), firstName, lastName, enabled, role (enum). Parents and students are not staff.
 */
@Component
public class JpqlStaffDirectory implements StaffDirectoryPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Staff> staff() {
        List<Object[]> rows = em.createQuery(
                "select u.email, u.firstName, u.lastName, cast(u.role as string) from UserReg u " +
                        "where u.enabled = true and cast(u.role as string) not in ('PARENT','STUDENT') " +   // CHECK role names
                        "order by u.firstName, u.lastName").getResultList();
        List<Staff> out = new ArrayList<>();
        for (Object[] r : rows) out.add(new Staff(String.valueOf(r[0]), r[1] + " " + r[2], String.valueOf(r[3])));
        return out;
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<String> emailsOfRoles(String... roles) {
        if (roles == null || roles.length == 0) return List.of();
        return em.createQuery(
                        "select u.email from UserReg u where u.enabled = true and u.email is not null " +
                                "and cast(u.role as string) in :roles")
                .setParameter("roles", List.of(roles)).getResultList();
    }

    @Override
    @Transactional(readOnly = true)
    public String nameOf(String username) {
        List<Object[]> rows = em.createQuery(
                        "select u.firstName, u.lastName from UserReg u where u.email = :e", Object[].class)
                .setParameter("e", username).setMaxResults(1).getResultList();
        return rows.isEmpty() ? username : rows.get(0)[0] + " " + rows.get(0)[1];
    }
}