package com.school.money.port;

import com.school.money.MoneyException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class JpqlStudentLedger implements StudentLedgerPort {

    @PersistenceContext
    private EntityManager em;

    private static BigDecimal bd(Object o) {
        return o == null ? BigDecimal.ZERO : new BigDecimal(o.toString());
    }

    private <T> T guard(java.util.function.Supplier<T> s) {
        try {
            return s.get();
        } catch (RuntimeException e) {
            if (e instanceof MoneyException) throw e;
            throw MoneyException.conflict("Refund ledger query failed - a field name in JpqlStudentLedger "
                    + "does not match your entities: " + rootMessage(e));
        }
    }

    private static String rootMessage(Throwable t) {
        while (t.getCause() != null) t = t.getCause();
        return t.getMessage();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal creditFor(Long studentId, Long termId) {
        return guard(() -> {
            Object paid = em.createQuery(
                            "select sum(t.amount) from MpesaTransactions t " +   // CHECK t.amount
                                    "where t.student.studentId = :sid and t.funds.term.termId = :tid " +  // CHECK ids
                                    "and cast(t.status as string) = 'SUCCESS'")        // CHECK t.status
                    .setParameter("sid", studentId).setParameter("tid", termId).getSingleResult();
            Object due = em.createQuery(
                            "select sum(f.amount) from Funds f, Student s " +
                                    "where s.studentId = :sid and f.classes = s.classes and f.term.termId = :tid")
                    .setParameter("sid", studentId).setParameter("tid", termId).getSingleResult();
            // Money carried forward from an earlier term counts as already paid toward this term.
            Object carried = em.createQuery(
                            "select sum(c.amount) from FeeCredit c where c.studentId = :sid and c.targetTermId = :tid")
                    .setParameter("sid", studentId).setParameter("tid", termId).getSingleResult();
            return bd(paid).add(bd(carried)).subtract(bd(due));
        });
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Long> studentsWithPayments(Long termId) {
        return guard(() -> (List<Long>) em.createQuery(
                        "select distinct t.student.studentId from MpesaTransactions t " +
                                "where t.funds.term.termId = :tid and cast(t.status as string) = 'SUCCESS'")
                .setParameter("tid", termId).getResultList());
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Payer> payersFor(Long studentId, Long termId) {
        return guard(() -> {
            List<Object[]> rows = em.createQuery(
                            "select t.phoneNumber, sum(t.amount), max(t.id) from MpesaTransactions t " + // CHECK phoneNumber
                                    "where t.student.studentId = :sid and t.funds.term.termId = :tid " +
                                    "and cast(t.status as string) = 'SUCCESS' group by t.phoneNumber order by max(t.id) desc")
                    .setParameter("sid", studentId).setParameter("tid", termId).getResultList();
            List<Payer> out = new ArrayList<>();
            for (Object[] r : rows) out.add(new Payer(String.valueOf(r[0]), bd(r[1])));
            return out;
        });
    }

    @Override
    @Transactional(readOnly = true)
    public String studentName(Long studentId) {
        return guard(() -> {
            Object[] r = (Object[]) em.createQuery(
                            "select s.firstName, s.lastName from Student s where s.studentId = :sid")
                    .setParameter("sid", studentId).getSingleResult();
            return r[0] + " " + r[1];
        });
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Long> studentIdsOfParent(String username) {
        return guard(() -> (List<Long>) em.createQuery(
                        "select s.studentId from Student s where s.parent.userReg.email = :u") // Parent.userReg -> UserReg.email
                .setParameter("u", username).getResultList());
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public Long nextTermId(Long termId) {
        return guard(() -> {
            List<Long> ids = em.createQuery(
                            "select t.termId from Term t where t.termId > :tid order by t.termId")
                    .setParameter("tid", termId).setMaxResults(1).getResultList();
            return ids.isEmpty() ? null : ids.get(0);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public String parentEmailOfStudent(Long studentId) {
        return guard(() -> {
            List<?> r = em.createQuery("select s.parent.userReg.email from Student s where s.studentId = :sid")
                    .setParameter("sid", studentId).setMaxResults(1).getResultList();
            return r.isEmpty() || r.get(0) == null ? null : r.get(0).toString();
        });
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<Long> recentTermIds() {
        return guard(() -> (List<Long>) em.createQuery("select t.termId from Term t order by t.termId desc")
                .setMaxResults(2).getResultList());
    }
}