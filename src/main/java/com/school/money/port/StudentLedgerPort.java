package com.school.money.port;

import java.math.BigDecimal;
import java.util.List;

/**
 * The only place where the money module reads the school's existing fee tables
 * (Funds, MpesaTransactions, Student, Term, Parent). If a field name differs in your project,
 * fix it in JpqlStudentLedger, nowhere else.
 */
public interface StudentLedgerPort {

    /** A person who paid fees for a student: the refund goes back to this number. */
    record Payer(String phone, BigDecimal paid) {}

    /** Total paid minus total due for the student in the term. Positive = overpaid. */
    BigDecimal creditFor(Long studentId, Long termId);

    /** Students that have at least one successful payment in the term. */
    List<Long> studentsWithPayments(Long termId);

    /** Payers of this student in this term, most recent payer first, each with what they paid. */
    List<Payer> payersFor(Long studentId, Long termId);

    String studentName(Long studentId);

    /** Student ids of the parent whose login name (email) is {@code username}. */
    List<Long> studentIdsOfParent(String username);

    /** The first term that starts after the given term, or null if none exists yet. */
    Long nextTermId(Long termId);

    /** Login email of the parent of this student, or null. */
    String parentEmailOfStudent(Long studentId);

    /** The two most recent terms (newest first): the ones scanned automatically for overpayments. */
    List<Long> recentTermIds();
}