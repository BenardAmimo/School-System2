package com.school.service;

import com.school.entity.Funds;
import com.school.entity.Student;
import com.school.repo.FundsRepository;
import com.school.repo.StudentRepo;
import com.school.request.FundsRequest;
import com.school.response.FundsResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FundsService implements FundsServe{
    private final StudentRepo studentRepo;
    private final FundsRepository fundsRepository;

    public FundsService(StudentRepo studentRepo, FundsRepository fundsRepository) {
        this.studentRepo = studentRepo;
        this.fundsRepository = fundsRepository;
    }

    @Override
    public FundsResponse createFunds(FundsRequest fundsRequest) {
        Student student = studentRepo.findById(fundsRequest.getStudentId())
                .orElseThrow(()->new RuntimeException("Student is not found"));

        Funds funds = new Funds();
        funds.setAmount(fundsRequest.getAmount());
        funds.setStudents(student);
        funds.setCreatedAt(LocalDateTime.now());
        //fundType
        Funds saved = fundsRepository.save(funds);

        FundsResponse response = new FundsResponse();
        response.setCreatedAt(saved.getCreatedAt());
        response.setFundsId(saved.getFundsId());
        response.setAmount(saved.getAmount());
        response.setStudentsName(saved.getStudents().getRegNo());

        return response;
    }
}
