package com.example.demo.service;

import com.example.demo.dto.EvaluateAnswerDTO;
import com.example.demo.dto.RatingDTO;
import com.example.demo.enums.AnswerStatus;
import com.example.demo.enums.ArtifactType;
import com.example.demo.exception.AnswerNotBelongsToCourseException;
import com.example.demo.exception.ArtifactNotBelongsToCourseException;
import com.example.demo.exception.IncorrectArtifactTypeException;
import com.example.demo.exception.NoRightsException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.model.Answer;
import com.example.demo.model.Artifact;
import com.example.demo.model.Course;
import com.example.demo.model.Grade;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.ArtifactRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.GradeRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class GradeService {
    private GradeRepository gradeRepository;
    private AnswerRepository answerRepository;
    private CourseRepository courseRepository;
    private ArtifactRepository artifactRepository;

    @CacheEvict(value = "grades", key = "#dto.courseId")
    public void evaluateAnswer(Integer user_id, EvaluateAnswerDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new NotFoundException("The course does not exist!"));
        Artifact artifact = artifactRepository.findById(dto.getArtifactId())
                .orElseThrow(() -> new NotFoundException("The artifact does not exist!"));
        Answer answer = answerRepository.findById(dto.getAnswerId())
                .orElseThrow(() -> new NotFoundException("The answer does not exist!"));

        if (!Objects.equals(course.getTeacher().getId(), user_id))
            throw new NoRightsException("You are not the creator of the course!");

        if (!checkIsArtifactBelongsToCourse(artifact, course))
            throw new ArtifactNotBelongsToCourseException("This artifact does not belong to the course!");

        if (!artifact.getArtifactType().equals(ArtifactType.TASK))
            throw new IncorrectArtifactTypeException(String.format("Artifact type %s cannot be evaluated!",
                                                     artifact.getArtifactType()));

        if (!Objects.equals(answer.getCourse().getId(), course.getId()) ||
            !Objects.equals(answer.getArtifact().getId(), artifact.getId())) {
            throw new AnswerNotBelongsToCourseException("This answer does not belong to the course!");
        }

        Grade grade = Grade.builder()
                .grade(dto.getGrade())
                .artifact(artifact)
                .course(course)
                .student(answer.getStudent())
                .build();

        gradeRepository.save(grade);

        answer.setStatus(AnswerStatus.CHECKED);
        answerRepository.save(answer);
    }

    @Cacheable(value = "grades", key = "#courseId")
    public List<RatingDTO> getRatingByCourseId(Integer courseId) {
        return gradeRepository.findRatingByCourseId(courseId);
    }

    private boolean checkIsArtifactBelongsToCourse(Artifact artifact, Course course) {
        return Objects.equals(artifact.getCourse().getId(), course.getId());
    }
}
