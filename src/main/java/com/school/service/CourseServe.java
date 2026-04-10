package com.school.service;

import com.school.request.CourseRequest;
import com.school.response.CourseResponse;

import java.util.List;

public interface CourseServe {

    CourseResponse createCourse(CourseRequest courseRequest);

    List<CourseResponse> fetchAllCourses();

    CourseResponse getByIdCourse(Long courseId);

    CourseResponse updateCourse(Long courseId, CourseRequest courseRequest);

    CourseResponse getByCourseName(String name);


    void deleteCourseById(Long courseId);

    void deleteAllCourses();
}
