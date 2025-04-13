package com.example.demo.contoller;

import com.example.demo.dto.FileDTO;
import com.example.demo.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("file")
@AllArgsConstructor
@Slf4j
public class FileController {
    private FileService fileService;

    @PostMapping("/download")
    @Operation(summary = "Получить файл по его имени")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE,
                    schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Файл <название файла> не найден!"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")})
    public ResponseEntity<Resource> downloadFile(@Validated @RequestBody FileDTO dto) throws IOException {
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
    }
}
