package com.example.demo.service;

import com.example.demo.annoration.Loggable;
import com.example.demo.annoration.Timed;
import com.example.demo.config.TopicConfig;
import com.example.demo.dto.AnswerDTO;
import com.example.demo.dto.GetAllAnswersDTO;
import com.example.demo.dto.GetAnswersDTO;
import com.example.demo.enums.AnswerStatus;
import com.example.demo.enums.AnswerType;
import com.example.demo.enums.ArtifactType;
import com.example.demo.exception.ArtifactNotBelongsToCourseException;
import com.example.demo.exception.FileStorageException;
import com.example.demo.exception.IncorrectArtifactTypeException;
import com.example.demo.exception.KafkaSendMessageException;
import com.example.demo.exception.NoRightsException;
import com.example.demo.mapper.AnswerMapper;
import com.example.demo.model.Answer;
import com.example.demo.model.Artifact;
import com.example.demo.model.Course;
import com.example.demo.model.User;
import com.example.demo.repository.AnswerRepository;
import com.example.demo.util.FileEventDtoCreator;
import com.example.demo.util.FileNameValidator;
import com.example.dto.FileEventDTO;
import com.example.dto.FileResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.lang.Nullable;
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
    private final EntityFindService entityFindService;
    private final AnswerMapper answerMapper;
    private final ObjectMapper mapper = new ObjectMapper();
    private KafkaTemplate<String, String> kafkaTemplate;
    private TopicConfig config;
    private FileNameValidator fileNameValidator;
    private FileEventDtoCreator fileEventDtoCreator;

    @CacheEvict(value = "answers", allEntries = true)
    @Loggable(process = "добавления ответа на задание")
    @Timed
    public void addAnswer(Integer userId, AnswerDTO dto, MultipartFile file) {
        Course course = entityFindService.findCourseById(dto.getCourseId());
        User user = entityFindService.findUserById(userId);
        Artifact artifact = entityFindService.findArtifactById(dto.getArtifactId());

        validateUserPermissions(course, user);
        validateArtifact(course, artifact);

        Answer answer = saveAnswer(dto, user, course, artifact);

        if (isFileAnswer(file, dto)) {
            storeAnswer(file, userId, artifact.getId(), course.getId(), answer.getId());
        }
    }

    private void validateUserPermissions(Course course, User user) {
        if (!checkIsRegisterOnCourse(course, user))
            throw new NoRightsException("You do not have permission to add an answer to this course!");
    }

    private void validateArtifact(Course course, Artifact artifact) {
        if (!checkIsArtifactBelongsToCourse(artifact, course))
            throw new ArtifactNotBelongsToCourseException("This artifact does not belong to the course!");
        if (!artifact.getArtifactType().equals(ArtifactType.TASK))
            throw new IncorrectArtifactTypeException(String.format("Artifact type %s cannot be answered!",
                    artifact.getArtifactType()));
    }

    private boolean isFileAnswer(@Nullable MultipartFile file, AnswerDTO dto) {
        return file != null && dto.getAnswerType() == AnswerType.FILE;
    }

    private Answer saveAnswer(AnswerDTO dto, User user, Course course, Artifact artifact) {
        Answer answer = createAnswer(dto, user, course, artifact);

        answerRepository.save(answer);

        return answer;
    }

    private Answer createAnswer(AnswerDTO dto, User user, Course course, Artifact artifact) {
        return Answer.builder()
                .submitDate(LocalDateTime.now())
                .content(dto.getContent())
                .answerType(dto.getAnswerType())
                .status(AnswerStatus.UNCHECKED)
                .student(user)
                .course(course)
                .artifact(artifact).build();
    }

    @Cacheable(value = "answers", key = "{#dto.courseId, #dto.status}")
    @Timed
    public List<GetAllAnswersDTO> getAllAnswersOfCourseByStatus(GetAnswersDTO dto, Integer userId) {
        Course course = entityFindService.findCourseById(dto.getCourseId());
        User user = entityFindService.findUserById(userId);

        checkIsCreatorOfCourse(course, user);

        return findAllAnswersByGetAnswersDTO(dto);
    }

    private void checkIsCreatorOfCourse(Course course, User user) {
        if (!Objects.equals(course.getTeacher().getId(), user.getId())) {
            throw new NoRightsException("You are not the creator of the course!");
        }
    }

    private List<GetAllAnswersDTO> findAllAnswersByGetAnswersDTO(GetAnswersDTO dto) {
        return answerRepository.findByAnswerTypeAndCourseIdAndArtifactType(dto.getStatus(),
                        dto.getCourseId(), ArtifactType.TASK).stream()
                .map(answerMapper::answerToAnswerDTO)
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

    private void storeAnswer(MultipartFile file, Integer userId,
                               Integer artifactId, Integer courseId, Integer answerId) {
        String filename = file.getOriginalFilename();

        fileNameValidator.validate(filename);

        String dir = String.format("%d/%s/%d/%d", courseId, "answers", userId, artifactId);
        log.info("Процесс: сохранение ответа в виде файла в директории. Директория файла {}", dir);

        FileEventDTO eventDTO = fileEventDtoCreator.create(file, dir, filename, answerId);

        sendFileEventDTO(eventDTO);
    }

    private void sendFileEventDTO(FileEventDTO dto) {
        try {
            kafkaTemplate.send(config.getFileEvents(), mapper.writeValueAsString(dto))
                    .thenAcceptAsync(result -> log.info("Sent message: {} with offset {}", dto,
                            result.getRecordMetadata().offset()))
                    .exceptionallyAsync(error -> {
                        throw new KafkaSendMessageException("Error sending message in Kafka!");
                    });
        } catch (JsonProcessingException e) {
            throw new FileStorageException("FileEvent DTO parse error!");
        }
    }

    @SneakyThrows
    @KafkaListener(topics = "${topics.file-answers}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleFileResponse(String response) {
        FileResponseDTO dto = mapper.readValue(response, FileResponseDTO.class);

        log.info("Получен асинхронный ответ для сохранения файла. CorrelationId: {}", dto.getArtifactId());

        answerRepository.findById(dto.getArtifactId())
                .ifPresent(answer -> {
                    answer.setContent(dto.getDir());
                    answerRepository.save(answer);
                    log.info("Обновлён ответ с ID {}: установлен путь к файлу {}", answer.getId(), dto.getDir());
                });
    }
}
