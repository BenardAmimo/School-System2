package com.school.repo;

import com.school.entity.Exam;
import com.school.entity.ExamPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;



@Repository
public interface ExamPeriodRepository extends JpaRepository<ExamPeriod, Long> {
    @Query("select p from ExamPeriod p join fetch p.term order by p.startDate desc")
    List<ExamPeriod> findAllWithTerm();

    @Query("""
        select e from Exam e
        join fetch e.assignment a
        join fetch a.teacher t join fetch t.userReg
        join fetch a.subject join fetch a.schoolClass
        join fetch e.examPeriod p join fetch p.term
        """ + " where e.examId = :id")
    Optional<Exam> findByIdWithDetails(Long id);
}
