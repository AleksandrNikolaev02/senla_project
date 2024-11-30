package com.example.demo.contoller;

import com.example.demo.dto.ArtifactDTO;
import com.example.demo.dto.FileDTO;
import com.example.demo.dto.GetArtifactDTO;
import com.example.demo.dto.SmallArtifactDTO;
import com.example.demo.exception.FileStorageException;
import com.example.demo.service.ArtifactService;
import com.example.demo.service.FileService;
import com.example.demo.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/artifact")
@AllArgsConstructor
@Slf4j
public class ArtifactController {
    private final ArtifactService artifactService;
    private final FileService fileService;
    private final Util util;

    @PreAuthorize("hasAnyAuthority('ADMIN', 'TEACHER')")
    @RequestMapping(value = "/add", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Добавить артефакт на курс")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Артефакт успешно добавлен!"),
            @ApiResponse(responseCode = "404", description = "Такого курса не существует!", content = @Content),
            @ApiResponse(responseCode = "404", description = "Такого пользователя не существует!", content = @Content),
            @ApiResponse(responseCode = "403", description = "Вы не являетесь создателем курса!", content = @Content)
    })
    public ResponseEntity<String> addArtifactToCourse(
            @Parameter(
                description = "Данные артефакта",
                required = true,
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            )
            @Validated @RequestPart("dto") ArtifactDTO dto,
            
            @Parameter(description = "Токен авторизации")
            @RequestHeader("Authorization") String token,
            
            @Parameter(
                description = "Файл для загрузки",
                required = false,
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestPart(value = "file", required = false) MultipartFile file) {
        Integer userId = util.getIdFromToken(token);
        dto.setFile(file);
        artifactService.addArtifactToCourse(userId, dto);
        return ResponseEntity.status(201).body("Artifact successfully added!");
    }


    @PostMapping("/download")
    @Operation(summary = "Получить файл по его имени")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Файл <название файла> не найден!"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<Resource> downloadArtifact(@Validated @RequestBody FileDTO dto) {
        try {
            Resource resource = fileService.getFile(dto.getFilename());

            String encodedFilename = UriUtils.encode(dto.getFilename(), StandardCharsets.UTF_8);
            String contentDisposition = String.format("attachment; filename*=UTF-8''%s", encodedFilename);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);

            Tika tika = new Tika();
            String mimeType = tika.detect(resource.getInputStream());
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(mimeType))
                    .body(resource);
        } catch (FileStorageException e) {
            log.error("Файл {} не найден!", dto.getFilename());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Ошибка при загрузке файла {}: {}", dto.getFilename(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/getArtifacts")
    @Operation(summary = "Получить список артифактов по типу и курсу")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         content = @Content(mediaType = "application/json",
                         array = @ArraySchema(schema = @Schema(implementation = SmallArtifactDTO.class)))) })
    public ResponseEntity<Page<SmallArtifactDTO>> getArtifactByTypeAndCourseId(
            @Validated @RequestBody GetArtifactDTO dto,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(artifactService.getArtifactByTypeAndCourseId(dto, pageable));
    }
}
