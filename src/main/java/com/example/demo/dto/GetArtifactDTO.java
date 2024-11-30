package com.example.demo.dto;

import java.io.Serializable;

import com.example.demo.enums.ArtifactType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class GetArtifactDTO implements Serializable{
    @NotNull(message = "Course ID cannot be null")
    @Min(value = 1, message = "Course ID must be greater than 0")
    private Integer courseId;
    private ArtifactType artifactType;
}
