package com.example.demo.service;

import com.example.demo.dto.AnswerDTO;
import com.example.demo.dto.GetAllAnswersDTO;
import com.example.demo.dto.GetAnswersDTO;
import com.example.demo.enums.AnswerStatus;
import com.example.demo.enums.AnswerType;
import com.example.demo.enums.ArtifactType;
import com.example.demo.exception.ArtifactNotBelongsToCourseException;
import com.example.demo.exception.FileStorageException;
import com.example.demo.exception.IncorrectArtifactTypeException;
import com.example.demo.exception.NoRightsException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.AnswerMapper;
import com.example.demo.model.Answer;
import com.example.demo.model.Artifact;
import com.example.demo.model.Course;
import com.example.demo.model.User;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.repository.ArtifactRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final ArtifactRepository artifactRepository;
    private final FileService fileService;
    private final AnswerMapper mapper;

    @CacheEvict(value = "answers", allEntries = true)
    public void addAnswer(Integer user_id, AnswerDTO dto, MultipartFile file) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new NotFoundException("The course does not exist!"));
        User user = userRepository.findById(user_id)
                .orElseThrow(() -> new NotFoundException("The user does not exist!"));
        Artifact artifact = artifactRepository.findById(dto.getArtifactId())
                .orElseThrow(() -> new NotFoundException("The artifact does not exist!"));

        if (!checkIsRegisterOnCourse(course, user))
            throw new NoRightsException("You do not have permission to add an answer to this course!");
        if (!checkIsArtifactBelongsToCourse(artifact, course))
            throw new ArtifactNotBelongsToCourseException("This artifact does not belong to the course!");
        if (!artifact.getArtifactType().equals(ArtifactType.TASK))
            throw new IncorrectArtifactTypeException(String.format("Artifact type %s cannot be answered!",
                                                     artifact.getArtifactType()));

        Answer answer = Answer.builder()
                            .submitDate(LocalDateTime.now())
                            .content(dto.getContent())
                            .answerType(dto.getAnswerType())
                            .status(AnswerStatus.UNCHECKED)
                            .student(user)
                            .course(course)
                            .artifact(artifact).build();

        if (file != null && dto.getAnswerType() == AnswerType.FILE) {
            String filePath = storeAnswer(file, user_id, artifact.getId(), course.getId());
            answer.setContent(filePath);
        }

        answerRepository.save(answer);

        log.info("Процесс: завершение добавления ответа на задание. Ответ успешно добавлен!");
    }

    @Cacheable(value = "answers", key = "{#dto.courseId, #dto.status}")
    public List<GetAllAnswersDTO> getAllAnswersOfCourseByStatus(GetAnswersDTO dto, Integer user_id) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new NotFoundException("The course does not exist!"));
        User user = userRepository.findById(user_id)
                .orElseThrow(() -> new NotFoundException("The user does not exist!"));

        if (!Objects.equals(course.getTeacher().getId(), user.getId()))
            throw new NoRightsException("You are not the creator of the course!");

        return answerRepository.findByAnswerTypeAndCourseIdAndArtifactType(dto.getStatus(),
                dto.getCourseId(), ArtifactType.TASK).stream()
                .map(mapper::answerToAnswerDTO)
                .collect(Collectors.toList());
    }

    private boolean checkIsRegisterOnCourse(Course course, User user) {
        Hibernate.initialize(user);
        List<Course> courses = user.getCourses();

        for (Course userCourse : courses) {
            if (Objects.equals(userCourse.getId(), course.getId())) return true;
        }

        return false;
    }

    private boolean checkIsArtifactBelongsToCourse(Artifact artifact, Course course) {
        log.info("Процесс: проверка принадлежности артифакта {} курсу {}.", artifact, course);
        return Objects.equals(artifact.getCourse().getId(), course.getId());
    }

    private String storeAnswer(MultipartFile file, Integer user_id, Integer artifactId, Integer courseId) {
        String filename = file.getOriginalFilename();

        if (filename == null && filename.contains("..")) {
            throw new FileStorageException("Invalid file path sequence: " + filename);
        }

        String dir = String.format("%d/%s/%d/%d", courseId, "answers", user_id, artifactId);
        log.info("Процесс: сохранение ответа в виде файла в директории. Директория файла {}", dir);

        return fileService.storeFile(dir, file);
    }
}
