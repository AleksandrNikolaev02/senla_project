package com.example.demo.mapper;

import com.example.demo.dto.CourseDTO;
import com.example.demo.dto.CreateCourseDTO;
import com.example.demo.model.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CourseMapper {
    Course courseDtoToCourse(CreateCourseDTO createCourseDTO);

    @Mapping(source = "teacher.id", target = "author_id")
    @Mapping(expression = "java(course.getTeacher().getSname() + \" \" + course.getTeacher().getFname())", target = "fio")
    CourseDTO courseToCourseDTO(Course course);
}
