package com.example.demo.repository;

import com.example.demo.enums.AnswerStatus;
import com.example.demo.enums.ArtifactType;
import com.example.demo.model.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    @Query("SELECT a FROM Answer a " +
            "WHERE (:answerStatus IS NULL OR a.status = :answerStatus) " +
            "AND a.course.id = :courseId " +
            "AND a.artifact.artifactType = :artifactType")
    List<Answer> findByAnswerTypeAndCourseIdAndArtifactType(@Param("answerStatus") AnswerStatus answerStatus,
                                                              @Param("courseId") Integer courseId,
                                                              @Param("artifactType") ArtifactType artifactType);
}
