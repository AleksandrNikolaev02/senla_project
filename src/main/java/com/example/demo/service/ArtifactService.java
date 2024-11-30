package com.example.demo.service;

import com.example.demo.dto.ArtifactDTO;
import com.example.demo.dto.GetArtifactDTO;
import com.example.demo.dto.SmallArtifactDTO;
import com.example.demo.enums.ArtifactType;
import com.example.demo.exception.FileStorageException;
import com.example.demo.exception.NoRightsException;
import com.example.demo.exception.NotFoundException;
import com.example.demo.mapper.ArtifactMapper;
import com.example.demo.model.Artifact;
import com.example.demo.model.Course;
import com.example.demo.model.User;
import com.example.demo.repository.ArtifactRepository;
import com.example.demo.repository.CourseRepository;
import com.example.demo.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@Service
@Slf4j
@AllArgsConstructor
public class ArtifactService {
    private final ArtifactRepository artifactRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final FileService fileService;
    private final ArtifactMapper mapper;

    @CacheEvict(value = "artifacts", allEntries = true)
    public void addArtifactToCourse(Integer user_id, ArtifactDTO dto) {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new NotFoundException("The course does not exist!"));
        User user = userRepository.findById(user_id)
                .orElseThrow(() -> new NotFoundException("The user does not exist!"));

        if (!Objects.equals(course.getTeacher().getId(), user.getId())) {
            throw new NoRightsException("You are not the creator of the course!");
        }

        Artifact artifact = Artifact.builder()
                .course(course)
                .artifactType(dto.getArtifactType())
                .title(dto.getTitle())
                .content(dto.getContent()).build();

        artifactRepository.save(artifact);

        log.info(artifact.toString());

        if (dto.getFile() != null && dto.getArtifactType() == ArtifactType.FILE) {
            String filePath = storeArtifact(course.getId(), artifact.getId(), dto.getFile());
            artifact.setContent(filePath);
        }

        artifactRepository.save(artifact);

        log.info("Процесс: добавление артифакта {} на курс {} завершено. Артифакт успешно добавлен!", artifact, course);
    }

    @Cacheable(value = "artifacts", key = "{#dto.courseId, #dto.artifactType != null ? #dto.artifactType : 'null'}")
    public Page<SmallArtifactDTO> getArtifactByTypeAndCourseId(GetArtifactDTO dto, Pageable pageable) {
        if (dto.getArtifactType() != null) return artifactRepository
                .findByArtifactTypeAndCourseId(dto.getArtifactType(), dto.getCourseId(), pageable)
                .map(mapper::artifactToSmallArtifactDTO);
        return artifactRepository.findAll(pageable).map(mapper::artifactToSmallArtifactDTO);
    }

    private String storeArtifact(Integer courseId, Integer artifactId, MultipartFile file) {
        String filename = file.getOriginalFilename();

        if (filename == null || filename.contains("..")) {
            throw new FileStorageException("Invalid file path sequence: " + filename);
        }

        String dir = courseId + "/" + "artifacts" + "/" +  artifactId;

        log.info("Процесс: сохранение артефакта в виде файла в директории. Директория файла {}", dir);

        return fileService.storeFile(dir, file);
    }
}
