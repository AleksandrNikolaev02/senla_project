package com.example.demo.repository;

import com.example.demo.dto.RatingDTO;
import com.example.demo.model.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Integer> {
    @Query("SELECT new com.example.demo.dto.RatingDTO(u.id, AVG(g.grade), u.fname, u.sname) " +
            "FROM Grade g LEFT JOIN User u ON g.student.id = u.id " +
            "WHERE g.course.id = :courseId GROUP BY u.id, u.fname, u.sname " +
            "ORDER BY AVG(g.grade) DESC")
    List<RatingDTO> findRatingByCourseId(@Param("courseId") Integer courseId);
}
