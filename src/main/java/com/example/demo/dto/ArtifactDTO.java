package com.example.demo.dto;

import com.example.demo.enums.ArtifactType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@Getter
@Setter
public class ArtifactDTO implements Serializable {
    @NotNull(message = "Course ID cannot be null")
    @Min(value = 1, message = "Course ID must be greater than 0")
    private Integer courseId;
    @NotNull(message = "Title cannot be null")
    private String title;
    @NotNull(message = "Artifact Type cannot be null")
    private ArtifactType artifactType;
    @NotNull(message = "Content cannot be null")
    private String content;
    @Schema(description = "Файл артефакта", type = "string", format = "binary")
    private MultipartFile file;
}
