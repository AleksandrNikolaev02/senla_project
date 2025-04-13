package com.example.demo.mapper;

import com.example.demo.dto.GetAllAnswersDTO;
import com.example.demo.model.Answer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AnswerMapper {
    @Mapping(source = "course.id", target = "courseId")
    @Mapping(source = "artifact.id", target = "artifactId")
    GetAllAnswersDTO answerToAnswerDTO(Answer answer);
}
