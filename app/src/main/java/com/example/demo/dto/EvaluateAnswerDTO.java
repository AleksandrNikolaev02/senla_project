package com.example.demo.dto;

import java.io.Serializable;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class EvaluateAnswerDTO implements Serializable {
    @NotNull(message = "Course ID cannot be null")
    @Min(value = 1, message = "Course ID must be greater than 0")
    private Integer courseId;

    @NotNull(message = "Artifact ID cannot be null")
    @Min(value = 1, message = "Artifact ID must be greater than 0")
    private Integer artifactId;

    @NotNull(message = "Answer ID cannot be null")
    @Min(value = 1, message = "Answer ID must be greater than 0")
    private Integer answerId;

    @NotNull(message = "Grade cannot be null")
    @Min(value = 1, message = "Grade must be greater than 0")
    @Max(value = 5, message = "Grade must be less than 6")
    private Integer grade;
}
