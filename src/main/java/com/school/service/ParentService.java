package com.school.service;

import com.school.entity.Parent;
import com.school.repo.ParentRepo;
import com.school.repo.StudentRepository;
import com.school.request.ParentRequest;
import com.school.response.ChildSummary;
import com.school.response.ParentResponse;
import com.school.security.entity.UserReg;
import com.school.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

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
        parentResponse.setEmail(parent.getUserReg().getEmail());

        return parentResponse;
    }

    @Override
    public ParentResponse updateParent(Long parentId, ParentRequest parentRequest) {
        Parent parentDB = parentRepo.findById(parentId).
                orElseThrow(()->new RuntimeException("parent Not available!"));

        UserReg user = userRepository.findById((parentRequest.getUserId()))
                .orElseThrow(()->new RuntimeException("User not found"));

/*
        if(Objects.nonNull(parentRequest.getEmail())&&!"".equalsIgnoreCase(parentRequest.getEmail())){
            parentDB.setEmail(parentRequest.user().getEmail());
        }*/
        parentDB.setUserReg(user);
        Parent parent = parentRepo.save(parentDB);

        ParentResponse respo = new ParentResponse();

                respo.setParentId(parent.getParentId());
                respo.setFirstName(parent.getUserReg().getFirstName());
                respo.setLastName(parent.getUserReg().getLastName());
                respo.setEmail(parent.getUserReg().getEmail());

        return respo;
    }

    @Override
    public ParentResponse getParentByName(String name) {
        return null;
    }

    @Override
    public ParentResponse deleteParent(Long studentId) {
        return null;
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
                        .className(s.getClasses() != null ? s.getClasses().getName() : "Unassigned")
                        .build())
                .toList();
    }
}


