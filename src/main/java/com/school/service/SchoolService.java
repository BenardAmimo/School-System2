package com.school.service;

import com.school.entity.Course;
import com.school.entity.School;
import com.school.entity.Student;
import com.school.entity.Teacher;
import com.school.repo.CourseRepo;
import com.school.repo.SchoolRepository;
import com.school.repo.StudentRepo;
import com.school.repo.TeacherRepo;
import com.school.request.SchoolRequest;
import com.school.response.SchoolResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class SchoolService implements SchoolServInter{
    private final SchoolRepository schoolRepoitory;

    @Override
    public SchoolResponse createNewSchool(SchoolRequest schoolRequest) {


            School school = new School();
            school.setName(schoolRequest.getName());
            school.setMotto(schoolRequest.getMotto());
            school.setVision(schoolRequest.getVision());

            School saved = schoolRepoitory.save(school);

            SchoolResponse respond = new SchoolResponse();
            respond.setSchoolId(saved.getSchoolId());
            respond.setName(saved.getName());
            respond.setMotto(saved.getMotto());
            respond.setVision(saved.getVision());

        return respond;
    }

    @Override
    public SchoolResponse getSchoolById(Long schoolId) {

        School school = schoolRepoitory.findById(schoolId).
                orElseThrow(()->new RuntimeException("school not found"));

        SchoolResponse resp = new SchoolResponse();
        resp.setSchoolId(school.getSchoolId());
        resp.setVision(school.getVision());
        resp.setMotto(school.getMotto());
        resp.setName(school.getName());
        return resp;
    }

    @Override
    public List<SchoolResponse> getAllSchools() {
        return schoolRepoitory.findAll().
                stream()
                .map(this::mapper)
                .toList();
    }

    @Override
    public void deleteById(Long schoolId) {
        School school = schoolRepoitory.findById(schoolId).
                orElseThrow(()->new RuntimeException("School not Found"));
         schoolRepoitory.delete(school);
    }

    @Override
    public void deleteAllSchools() {
        schoolRepoitory.deleteAll();
    }

    private SchoolResponse mapper(School school) {

        SchoolResponse respond = new SchoolResponse();
        respond.setSchoolId(school.getSchoolId());
        respond.setName(school.getName());
        respond.setMotto(school.getMotto());
        respond.setVision(school.getVision());


        return respond;
    }

}
