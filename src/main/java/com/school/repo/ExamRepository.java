package com.school.repo;

import com.school.entity.Exam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ExamRepository extends JpaRepository<Exam,Long> {
    @Query("""
    select e from Exam e
    join fetch e.assignment a
    join fetch a.teacher t join fetch t.userReg
    join fetch a.subject
    join fetch a.classes
    join fetch e.examPeriod p join fetch p.term
    order by e.examDate, e.startTime
    """)
    List<Exam> findAllWithDetails();

    @Query("""
    select e from Exam e
    join fetch e.assignment a
    join fetch a.teacher t join fetch t.userReg
    join fetch a.subject
    join fetch a.classes
    join fetch e.examPeriod p join fetch p.term
    where p.examPeriodId = :periodId
    order by e.examDate, e.startTime
    """)
    List<Exam> findByPeriodWithDetails(Long periodId);

    @Query("""
    select e from Exam e
    join fetch e.assignment a
    join fetch a.teacher t join fetch t.userReg
    join fetch a.subject
    join fetch a.classes
    join fetch e.examPeriod p join fetch p.term
    where e.examId = :id
    """)
    Optional<Exam> findByIdWithDetails(Long id);

    @Query("""
        select e from Exam e join fetch e.assignment a join fetch a.classes
        where e.examPeriod.examPeriodId = :periodId
        """)
    List<Exam> findByPeriodBasic(Long periodId);

    @Query("""
        select count(e) > 0 from Exam e
        where e.assignment.assignmentId = :assignmentId
          and e.examPeriod.examPeriodId = :periodId
        """)
    boolean existsForAssignment(Long assignmentId, Long periodId);

    @Query("""
        select count(e) > 0 from Exam e
        where e.assignment.classes.classesId = :classId
          and e.examDate = :date
          and e.startTime < :end and e.endTime > :start
          and e.examId <> :excludeId
        """)
    boolean existsClash(Long classId, LocalDate date, LocalTime start, LocalTime end, Long excludeId);

}
