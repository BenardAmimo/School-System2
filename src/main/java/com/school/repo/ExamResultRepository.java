package com.school.repo;

import com.school.entity.ExamResult;
import com.school.entity.ResultStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ExamResultRepository extends JpaRepository<ExamResult, Long> {

    boolean existsByExamExamId(Long examId);

    @Query("""
        select r from ExamResult r
        join fetch r.student s join fetch s.userReg
        where r.exam.examId = :examId
        """)
    List<ExamResult> findByExamWithStudents(Long examId);

    @Query("""
        select r from ExamResult r
        join fetch r.student s join fetch s.userReg
        join fetch r.exam e join fetch e.assignment a join fetch a.subject
        join fetch e.examPeriod p
        where a.schoolClass.classesId = :classId
          and p.term.termId = :termId
          and r.status = :status
        """)
    List<ExamResult> findByClassAndTermAndStatus(Long classId, Long termId, ResultStatus status);
}
