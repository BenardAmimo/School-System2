package com.school.service;
import com.school.entity.Parent;
import com.school.entity.Student;
import com.school.repo.ParentRepo;
import com.school.repo.SchoolClassesRepository;
import com.school.repo.StudentRepository;
import com.school.request.StudentRequest;
import com.school.response.StudentResponse;
import org.springframework.stereotype.Service;

@Service
public class StudentsService implements StudentsServ{
    private final StudentRepository studentRepository;
    private final SchoolClassesRepository repository;
    private final ParentRepo parentRepo;

    public StudentsService(StudentRepository studentRepository, SchoolClassesRepository repository, ParentRepo parentRepo) {
        this.studentRepository = studentRepository;
        this.repository = repository;
        this.parentRepo = parentRepo;
    }

    @Override
    public StudentResponse createStudents(StudentRequest studentRequest) {

        Parent parent = parentRepo.findById(studentRequest.getParentId())
                .orElseThrow(()->new RuntimeException("parent not Found"));


        Student student = new Student();
        student.setFirstName(studentRequest.getFirstName());
        student.setLastName(studentRequest.getLastName());


        Student saved = studentRepository.save(student);

        StudentResponse studentResponse = new StudentResponse();
        studentResponse.setStudentId(saved.getStudentId());
        studentResponse.setFirstName(saved.getFirstName());
        studentResponse.setLastName(saved.getLastName());

        return studentResponse;
    }
}
