package com.school.service;

import com.school.entity.Parent;
import com.school.entity.Student;
import com.school.repo.ParentRepo;
import com.school.repo.StudentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * One place that answers "may this user see this student's records?".
 * Use it from FundsService, AttendanceService and anything else keyed by studentId.
 * URL rules in SecurityConfig only check the ROLE; they cannot check ownership.
 */
@Service
public class StudentAccessPolicy {

    private final StudentRepository studentRepo;
    private final ParentRepo parentRepo;

    public StudentAccessPolicy(StudentRepository studentRepo, ParentRepo parentRepo) {
        this.studentRepo = studentRepo;
        this.parentRepo = parentRepo;
    }

    @Transactional(readOnly = true)
    public void assertCanView(Long studentId, Authentication auth, boolean teachersAllowed) {
        Set<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_SUPER_ADMIN")) return;

        // TODO: restrict teachers to students in classes they actually teach.
        if (teachersAllowed && roles.contains("ROLE_TEACHER")) return;

        if (roles.contains("ROLE_PARENT")) {
            Parent parent = parentRepo.findByUserReg_Email(auth.getName()).orElse(null);
            Student student = studentRepo.findById(studentId).orElse(null);
            if (parent != null && student != null && student.getParent() != null
                    && parent.getParentId().equals(student.getParent().getParentId())) {
                return;
            }
        }

        // Same error for "not yours" and "does not exist", so ids cannot be probed.
        throw new AccessDeniedException("You do not have access to this student's records.");
    }
}
