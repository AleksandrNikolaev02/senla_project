package com.example.demo.repository;

import com.example.demo.model.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface CourseRepository extends JpaRepository<Course, Integer> {
    Page<Course> findByTitleContaining(String title, Pageable pageable);
    @Query("SELECT c FROM Course c LEFT JOIN User u ON c.teacher.id = u.id WHERE CONCAT(u.sname, ' ', u.fname) LIKE %:name%")
    Page<Course> findByTeacherName(@Param("name") String teacherName, Pageable pageable);
    @Query("SELECT c FROM Course c LEFT JOIN User u ON c.teacher.id = u.id WHERE CONCAT(u.sname, ' ', u.fname) LIKE %:name% AND c.title LIKE %:title%")
    Page<Course> findByTitleAndTeacherName(@Param("title") String title, @Param("name") String teacherName, Pageable pageable);
}
