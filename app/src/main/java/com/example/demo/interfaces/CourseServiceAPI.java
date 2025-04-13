package com.example.demo.interfaces;

import com.example.demo.dto.UpdateCourseDTO;
import com.example.demo.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CourseServiceAPI {
    Page<Course> getCourses(String title, String teacherName, Pageable pageable);
    void createCourse(Course course, Integer userId);
    void updateCourse(UpdateCourseDTO dto, Integer user_id);
    void deleteCourse(Integer course_id, Integer user_id);
}

