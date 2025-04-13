package com.example.demo.service;

import com.example.demo.annoration.Loggable;
import com.example.demo.config.TopicConfig;
import com.example.demo.dto.ArtifactDTO;
import com.example.demo.dto.GetArtifactDTO;
import com.example.demo.dto.SmallArtifactDTO;
import com.example.demo.enums.ArtifactType;
import com.example.demo.exception.FileStorageException;
import com.example.demo.exception.KafkaSendMessageException;
import com.example.demo.exception.NoRightsException;
import com.example.demo.mapper.ArtifactMapper;
import com.example.demo.model.Artifact;
import com.example.demo.model.Course;
import com.example.demo.model.User;
import com.example.demo.repository.ArtifactRepository;
import com.example.demo.util.FileEventDtoCreator;
import com.example.demo.util.FileNameValidator;
import com.example.dto.FileEventDTO;
import com.example.dto.FileResponseDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
@Slf4j
@AllArgsConstructor
public class ArtifactService {
    private final ArtifactRepository artifactRepository;
    private final ArtifactMapper artifactMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private TopicConfig config;
    private EntityFindService entityFindService;
    private FileNameValidator fileNameValidator;
    private FileEventDtoCreator fileEventDtoCreator;

    @CacheEvict(value = "artifacts", allEntries = true)
    @Loggable
    public void addArtifactToCourse(Integer userId, ArtifactDTO dto) {
        Course course = entityFindService.findCourseById(dto.getCourseId());
        User user = entityFindService.findUserById(userId);

        validateUserPermissionsOnAddArtifactToCourse(course, user);

        Artifact artifact = createArtifactFromCourseAndArtifactDTO(course, dto);

        saveArtifact(artifact);

        if (isFileArtifact(dto)) {
            storeArtifact(course.getId(), artifact.getId(), dto.getFile());
        }

        log.info("Процесс: добавление артифакта {} на курс {} завершено. Артифакт успешно добавлен!",
                artifact, course);
    }

    private void validateUserPermissionsOnAddArtifactToCourse(Course course, User user) {
        if (!Objects.equals(course.getTeacher().getId(), user.getId())) {
            throw new NoRightsException("You are not the creator of the course!");
        }
    }

    private Artifact createArtifactFromCourseAndArtifactDTO(Course course, ArtifactDTO dto) {
        return Artifact.builder()
                .course(course)
                .artifactType(dto.getArtifactType())
                .title(dto.getTitle())
                .content(dto.getContent()).build();
    }

    private boolean isFileArtifact(ArtifactDTO dto) {
        return dto.getFile() != null && dto.getArtifactType() == ArtifactType.FILE;
    }

    private void saveArtifact(Artifact artifact) {
        artifactRepository.save(artifact);
    }

    @Cacheable(value = "artifacts", key = "{#dto.courseId, #dto.artifactType != null ? #dto.artifactType : 'null'}")
    public Page<SmallArtifactDTO> getArtifactByTypeAndCourseId(GetArtifactDTO dto, Pageable pageable) {
        if (dto.getArtifactType() != null) return artifactRepository
                .findByArtifactTypeAndCourseId(dto.getArtifactType(), dto.getCourseId(), pageable)
                .map(artifactMapper::artifactToSmallArtifactDTO);
        return artifactRepository.findAll(pageable).map(artifactMapper::artifactToSmallArtifactDTO);
    }

    @Loggable
    private void storeArtifact(Integer courseId, Integer artifactId, MultipartFile file) {
        String filename = file.getOriginalFilename();

        fileNameValidator.validate(filename);

        String dir = courseId + "/" + "artifacts" + "/" +  artifactId;

        log.info("Процесс: сохранение артефакта в виде файла в директории. Директория файла {}", dir);

        FileEventDTO eventDTO = fileEventDtoCreator.create(file, dir, filename, artifactId);

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
    @KafkaListener(topics = "${topics.file-responses}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleFileResponse(String response) {
        FileResponseDTO dto = mapper.readValue(response, FileResponseDTO.class);

        log.info("Получен асинхронный ответ для сохранения файла. CorrelationId: {}", dto.getArtifactId());

        artifactRepository.findById(dto.getArtifactId())
                .ifPresent(artifact -> {
                    artifact.setContent(dto.getDir());
                    artifactRepository.save(artifact);
                    log.info("Обновлён артефакт с ID {}: установлен путь к файлу {}", artifact.getId(), dto.getDir());
        });
    }
}
