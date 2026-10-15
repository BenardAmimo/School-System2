package com.school.service;

import com.school.entity.Parent;
import com.school.error.ParentNotFoundtException;
import com.school.repo.ParentRepo;
import com.school.repo.StudentRepository;
import com.school.request.ParentRequest;
import com.school.response.ChildSummary;
import com.school.response.ParentResponse;
import com.school.security.entity.UserReg;
import com.school.security.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParentService implements ParentServe {
    private final ParentRepo parentRepo;
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;



    @Override
    public List<ParentResponse> getAllParents() {
        return parentRepo.findAll()
                .stream()
                .map(ParentResponse::from)
                .toList();
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ParentResponse getParentById(Long parentId) {
        Parent parent = parentRepo.findById(parentId)
                .orElseThrow(() -> new ParentNotFoundtException("Parent " + parentId + " not found"));
        return ParentResponse.from(parent);
    }

    @Override
    @Transactional
    public ParentResponse updateParent(Long parentId, ParentRequest req) {
        Parent parent = parentRepo.findById(parentId)
                .orElseThrow(() -> new ParentNotFoundtException("Parent " + parentId + " not found"));
        UserReg user = parent.getUserReg();

        String email = req.email().trim().toLowerCase();
        if (!email.equalsIgnoreCase(user.getEmail())) {
            userRepository.findByEmail(email).ifPresent(other -> {
                throw new IllegalStateException("That email is already in use.");
            });
            user.setEmail(email);
        }
        user.setFirstName(req.firstName().trim());
        user.setLastName(req.lastName().trim());
        parent.setPhoneNumber(req.phoneNumber().trim());
        parent.setGender(req.gender());
        parent.setAge(req.age());

        return ParentResponse.from(parent);
    }
    @Override
    public ParentResponse getParentByName(String name) {
        return null;
    }

    @Override
    public void deleteParent(Long studentId) {
        Parent parent = parentRepo.findById(studentId)
                .orElseThrow(()->new RuntimeException("The parent is not in the System"));

        parentRepo.delete(parent);
    }

    @Override
    public List<ChildSummary> getMyChildren(String email) {
        Parent parent = parentRepo.findByUserReg_Email(email)
                .orElseThrow(() -> new RuntimeException("This account is not linked to a parent record"));

        return studentRepository.findByParent_ParentId(parent.getParentId()).stream()
                .map(s -> ChildSummary.builder()
                        .studentId(s.getStudentId())
                        .firstName(s.getFirstName())
                        .lastName(s.getLastName())
                        .age(s.getAge())
                        .className(s.getClasses() != null ? s.getClasses().getName() : "Unassigned")
                        .build())
                .toList();
    }
}


