package com.school.controller;

import com.example.demo.config.error.CourseNotFoundException;
import com.school.entity.Course;
import com.school.request.CourseRequest;
import com.school.response.CourseResponse;
import com.school.service.CourseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@Slf4j
public class CourseController {
    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping("/courses")
    public ResponseEntity<CourseResponse> createCourse(@RequestBody CourseRequest courseRequest){
        log.info("POST request to save course: {}",courseRequest.getName());

        CourseResponse fun = courseService.createCourse(courseRequest);

        log.info("Course saved with id{}",fun.getCourseId());

        return ResponseEntity.status(201).body(fun);
    }

    @GetMapping("/courses")
    public ResponseEntity<List<CourseResponse>> fetchAllCourses(){
        log.info("Get request for courses");

        List<CourseResponse> courses = courseService.fetchAllCourses();

        if (courses == null) {
            courses = new ArrayList<>(); //  prevent null
        }

        log.info("Found {} courses",courses.size());

        return ResponseEntity.ok(courses);
    }

    @GetMapping("/course/id/{courseId}")
    public ResponseEntity<CourseResponse> getByIdCourse(@PathVariable("courseId")Long courseId) throws CourseNotFoundException {
        log.info("Get request to get Course with id:{} ",courseId);

        CourseResponse gett = courseService.getByIdCourse(courseId);

        log.info("Course found with id {} ",gett.getCourseId());
        return ResponseEntity.ok(gett);
    }

    // In CourseController


    @PutMapping("/course/{courseId}")
    public ResponseEntity<CourseResponse> updateCourse(@PathVariable("courseId")Long courseId ,
                                               @RequestBody CourseRequest courseRequest){
        log.info("Updating courseName {} ",courseRequest.getName());

        CourseResponse updating = courseService.updateCourse(courseId,courseRequest);

        log.info("Course updated with id {} ",updating.getCourseId());
        return ResponseEntity.status(202).body(updating);

    }

    @GetMapping("/course/{name}")
    public ResponseEntity<CourseResponse> getByCourseName(@PathVariable("name")String name){

        log.info("Get request with courseName {} ",name);

        CourseResponse fun = courseService.getByCourseName(name);

        log.info("Course Found with id {} ",fun.getCourseId());
        return ResponseEntity.ok(fun);
    }

    @DeleteMapping("/course/{courseId}")
    public ResponseEntity<String>deleteCourseById(@PathVariable("CourseId")Long id){
        log.info("Delete request for course with id {} ",id);

        courseService.deleteCourseById(id);

        return ResponseEntity.ok("Successfully deleted");
    }

    @DeleteMapping("/deleteAllCourse")
    public ResponseEntity<String>deleteAllCourses(){
        log.info("Delete request for all courses");
         courseService.deleteAllCourses();
        return ResponseEntity.ok("All courses Have been deleted");
    }

}
