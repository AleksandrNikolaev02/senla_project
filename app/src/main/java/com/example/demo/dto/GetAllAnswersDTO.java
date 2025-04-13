package com.example.demo.dto;

import java.io.Serializable;

import com.example.demo.enums.AnswerType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class GetAllAnswersDTO implements Serializable {
    private Integer id;
    private AnswerType answerType;
    private Integer courseId;
    private Integer artifactId;
    private String content;
}
