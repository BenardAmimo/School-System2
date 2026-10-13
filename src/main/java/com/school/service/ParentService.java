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
                .map(this::mapToDto)
                .toList();
    }

    private ParentResponse mapToDto(Parent parent) {
        ParentResponse parenting = new ParentResponse();
        parenting.setParentId(parent.getParentId());
        parenting.setFirstName(parent.getUserReg().getLastName());
        parenting.setLastName(parent.getUserReg().getFirstName());
        parenting.setAge(parent.getAge());
        parenting.setGender(parent.getGender());
        parenting.setPhoneNumber(parent.getPhoneNumber());
        parenting.setEmail(parent.getUserReg().getEmail());

        return parenting;
    }

    @Override
    public ParentResponse getParentById(Long parentId) {
        Parent parent = parentRepo.findById(parentId).
                orElseThrow(()->new RuntimeException("Parent not found!"));

        ParentResponse parentResponse = new ParentResponse();
        parentResponse.setParentId(parent.getParentId());
        parentResponse.setFirstName(parent.getUserReg().getFirstName());
        parentResponse.setLastName(parent.getUserReg().getLastName());
        parentResponse.setGender(parent.getGender());
        parentResponse.setAge(parent.getAge());
        parentResponse.setPhoneNumber(parent.getPhoneNumber());
        parentResponse.setEmail(parent.getUserReg().getEmail());

        return parentResponse;
    }

    @Override
    @Transactional
    public ParentResponse updateParent(Long parentId, ParentRequest req) {
        Parent parent = parentRepo.findById(parentId)
                .orElseThrow(() -> new ParentNotFoundtException("Parent " + parentId + " not found"));
        UserReg user = parent.getUserReg();

        String email = req.getEmail().trim().toLowerCase();
        if (!email.equalsIgnoreCase(user.getEmail())) {
            userRepository.findByEmail(email).ifPresent(other -> {
                throw new IllegalStateException("That email is already in use.");
            });
            user.setEmail(email);
        }
        user.setFirstName(req.getFirstName().trim());
        user.setLastName(req.getLastName().trim());
        parent.setPhoneNumber(req.getPhoneNumber().trim());
        parent.setGender(req.getGender());
        parent.setAge(req.getAge());

        ParentResponse resp = new ParentResponse();
        resp.setParentId(parent.getParentId());
        resp.setFirstName(user.getFirstName());
        resp.setLastName(user.getLastName());
        resp.setPhoneNumber(parent.getPhoneNumber());
        resp.setGender(parent.getGender());
        resp.setAge(parent.getAge());
        resp.setEmail(user.getEmail());
        return resp;
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


