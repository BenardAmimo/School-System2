package com.school.service;

import com.school.entity.Course;
import com.school.repo.CourseRepo;
import com.school.request.CourseRequest;
import com.school.response.CourseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class CourseService implements CourseServe {
    private final CourseRepo courseRepo;


    @Override
    public CourseResponse createCourse(CourseRequest courseRequest) {

        Course course = new Course();
        course.setName(courseRequest.getName());
        course.setDescription(courseRequest.getDescription());

        Course saved = courseRepo.save(course);

        CourseResponse response = new CourseResponse();
        response.setCourseId(saved.getCourseId());
        response.setName(saved.getName());
        response.setDescription(saved.getDescription());

        return response;
    }

    @Override
    public List<CourseResponse> fetchAllCourses() {
        return courseRepo.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private CourseResponse mapToDto(Course course) {
        CourseResponse res = new CourseResponse();
        res.setCourseId(course.getCourseId());
        res.setName(course.getName());
        res.setDescription(course.getDescription());

        return res;
    }

    @Override
    public CourseResponse getByIdCourse(Long courseId) {
        Course course = courseRepo.findById(courseId).
                orElseThrow(() -> new RuntimeException("Course Not found"));

        CourseResponse courseResponse = new CourseResponse();

        courseResponse.setCourseId(course.getCourseId());
        courseResponse.setName(course.getName());
        courseResponse.setDescription(course.getDescription());

        return courseResponse;
    }

    @Override
    public CourseResponse updateCourse(Long courseId, CourseRequest courseRequest) {
        Course courseDB = courseRepo.findById(courseId).
                orElseThrow(()->new RuntimeException("Course not found"));

        if(Objects.nonNull(courseRequest.getName())&&!"".
                equalsIgnoreCase(courseRequest.getName())){
            courseDB.setName(courseRequest.getName());
        }

        if (Objects.nonNull(courseRequest.getDescription())&&!"".
                equalsIgnoreCase(courseRequest.getDescription())){
            courseDB.setDescription(courseRequest.getDescription());
        }
        Course saved = courseRepo.save(courseDB);
        CourseResponse respo = new CourseResponse();
        respo.setCourseId(saved.getCourseId());
        respo.setName(saved.getName());
        respo.setDescription(saved.getDescription());

        return respo;
    }

    @Override
    public CourseResponse getByCourseName(String name) {
        Course course = courseRepo.findByNameIgnoreCase(name);

        CourseResponse resp = new CourseResponse();

        resp.setCourseId(course.getCourseId());
        resp.setName(course.getName());
        resp.setDescription(course.getDescription());
        return resp;
    }

    @Override
    public void deleteCourseById(Long courseId) {

        Course course = courseRepo.findById(courseId).
                orElseThrow(()->new RuntimeException("Course Not found"));

        courseRepo.delete(course);
    }

    @Override
    public void deleteAllCourses() {
       courseRepo.deleteAll();
    }


}
