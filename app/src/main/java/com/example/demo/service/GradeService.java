package com.example.demo.service;

import com.example.demo.annoration.Loggable;
import com.example.demo.annoration.Timed;
import com.example.demo.dto.EvaluateAnswerDTO;
import com.example.demo.dto.RatingDTO;
import com.example.demo.enums.AnswerStatus;
import com.example.demo.enums.ArtifactType;
import com.example.demo.exception.AnswerNotBelongsToCourseException;
import com.example.demo.exception.ArtifactNotBelongsToCourseException;
import com.example.demo.exception.IncorrectArtifactTypeException;
import com.example.demo.exception.NoRightsException;
import com.example.demo.model.Answer;
import com.example.demo.model.Artifact;
import com.example.demo.model.Course;
import com.example.demo.model.Grade;
import com.example.demo.repository.AnswerRepository;
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
    private EntityFindService entityFindService;

    @CacheEvict(value = "grades", key = "#dto.courseId")
    @Loggable(process = "оценка ответа пользователя")
    @Timed
    public void evaluateAnswer(Integer userId, EvaluateAnswerDTO dto) {
        Course course = entityFindService.findCourseById(dto.getCourseId());
        Artifact artifact = entityFindService.findArtifactById(dto.getArtifactId());
        Answer answer = entityFindService.findAnswerById(dto.getAnswerId());

        validateEvaluationAnswer(userId, course, artifact, answer);

        Grade grade = createGradeEntity(dto, artifact, course, answer);

        saveEvaluationAnswerResult(grade, answer);
    }

    @Cacheable(value = "grades", key = "#courseId")
    @Timed
    public List<RatingDTO> getRatingByCourseId(Integer courseId) {
        return gradeRepository.findRatingByCourseId(courseId);
    }

    private void validateEvaluationAnswer(Integer userId, Course course, Artifact artifact, Answer answer) {
        if (!Objects.equals(course.getTeacher().getId(), userId))
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
    }

    private Grade createGradeEntity(EvaluateAnswerDTO dto, Artifact artifact,
                                    Course course, Answer answer) {
        return Grade.builder()
                .grade(dto.getGrade())
                .artifact(artifact)
                .course(course)
                .student(answer.getStudent())
                .build();
    }

    private void saveEvaluationAnswerResult(Grade grade, Answer answer) {
        gradeRepository.save(grade);
        answer.setStatus(AnswerStatus.CHECKED);
        answerRepository.save(answer);
    }

    private boolean checkIsArtifactBelongsToCourse(Artifact artifact, Course course) {
        return Objects.equals(artifact.getCourse().getId(), course.getId());
    }
}
