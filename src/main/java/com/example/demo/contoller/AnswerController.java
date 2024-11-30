package com.example.demo.contoller;

import com.example.demo.dto.AnswerDTO;
import com.example.demo.dto.GetAllAnswersDTO;
import com.example.demo.dto.GetAnswersDTO;
import com.example.demo.service.AnswerService;
import com.example.demo.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/answers")
public class AnswerController {
    private final AnswerService answerService;
    private final Util util;

    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Добавить ответ на задание")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Ответ успешно загружен!",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Такого курса не существует!",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Такого пользователя не существует!",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Такого артифакта не существует!",
                    content = @Content),
            @ApiResponse(responseCode = "403", description = "У вас нет прав на добавление ответа на данный курс!",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Данный артифакт не принадлежит курсу!",
                    content = @Content)})
    public ResponseEntity<String> addAnswer(@RequestHeader("Authorization") String token,
                                            @Validated @RequestPart("dto") AnswerDTO dto,
                                            @RequestPart(value = "file", required = false) MultipartFile file) {
        Integer userId = util.getIdFromToken(token);
        answerService.addAnswer(userId, dto, file);
        return ResponseEntity.status(201).body("Answer successfully loaded!");
    }

    @PostMapping("/getAnswers")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    @Operation(summary = "Получить список всех ответов")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = GetAllAnswersDTO.class)))),
            @ApiResponse(responseCode = "404", description = "Такого курса не существует!", content = @Content),
            @ApiResponse(responseCode = "404", description = "Такого пользователя не существует!", content = @Content),
            @ApiResponse(responseCode = "403", description = "Вы не являетесь создателем курса!", content = @Content)})
    public ResponseEntity<List<GetAllAnswersDTO>> getAnswers(@RequestHeader("Authorization") String token,
                                                             @Validated @RequestBody GetAnswersDTO dto) {
        Integer userId = util.getIdFromToken(token);
        List<GetAllAnswersDTO> answers = answerService.getAllAnswersOfCourseByStatus(dto, userId);
        return ResponseEntity.ok(answers);
    }
}
