package com.example.demo.contoller;

import com.example.demo.dto.EvaluateAnswerDTO;
import com.example.demo.dto.GetRatingDTO;
import com.example.demo.dto.RatingDTO;
import com.example.demo.service.GradeService;
import com.example.demo.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/grades")
public class GradeController {
    private final GradeService gradeService;
    private final Util util;

    @PostMapping("/evaluate")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    @Operation(summary = "Оценить отправленный ответ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Работа оценена!"),
            @ApiResponse(responseCode = "404", description = "Такого курса не существует!"),
            @ApiResponse(responseCode = "404", description = "Такого артифакта не существует!"),
            @ApiResponse(responseCode = "404", description = "Такого ответа не существует!"),
            @ApiResponse(responseCode = "403", description = "Вы не являетесь создателем курса!"),
            @ApiResponse(responseCode = "400", description = "Данный артифакт не принадлежит курсу!"),
            @ApiResponse(responseCode = "400", description = "Тип артифакта <artifact> нельзя оценить!"),
            @ApiResponse(responseCode = "400", description = "Данный ответ не принадлежит курсу!")})
    public ResponseEntity<String> evaluateAnswer(@RequestHeader("Authorization") String token,
                                                 @Validated @RequestBody EvaluateAnswerDTO dto) {
        Integer userId = util.getIdFromToken(token);

        gradeService.evaluateAnswer(userId, dto);

        return ResponseEntity.status(200).body("Answer evaluated!");
    }

    @PostMapping("/getRating")
    @Operation(summary = "Получить рейтинг всех учеников курса")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         content = @Content(mediaType = "application/json",
                         array = @ArraySchema(schema = @Schema(implementation = RatingDTO.class))))})
    public ResponseEntity<List<RatingDTO>> getRating(@Validated @RequestBody GetRatingDTO dto) {
        List<RatingDTO> rating = gradeService.getRatingByCourseId(dto.getCourseId());
        return ResponseEntity.ok(rating);
    }
}
