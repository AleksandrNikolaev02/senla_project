package com.example.demo.dto;

import java.io.Serializable;

import com.example.demo.enums.AnswerType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class AnswerDTO implements Serializable {
    @NotNull(message = "Answer Type cannot be null")
    private AnswerType answerType;
    @NotNull(message = "Course ID cannot be null")
    @Min(value = 1, message = "Course ID must be greater than 0")
    private Integer courseId;
    @NotNull(message = "Artifact ID cannot be null")
    @Min(value = 1, message = "Artifact ID must be greater than 0")
    private Integer artifactId;
    @NotNull(message = "Content cannot be null")
    private String content;
}
