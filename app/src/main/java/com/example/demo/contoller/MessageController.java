package com.example.demo.contoller;

import com.example.demo.dto.GetMessageDTO;
import com.example.demo.dto.MessageDTO;
import com.example.demo.service.MessageService;
import com.example.demo.util.Util;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/messages")
public class MessageController {
    private final MessageService messageService;
    private final Util util;

    @PostMapping("/send")
    @Operation(summary = "Отправить сообщение пользователю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Сообщение успешно отправлено!"),
            @ApiResponse(responseCode = "404", description = "Sender not found"),
            @ApiResponse(responseCode = "404", description = "Recipient not found")})
    public ResponseEntity<String> sendMessage(@Validated @RequestBody MessageDTO dto,
                                              @RequestHeader(name = "Authorization") String token) {
        Integer senderId = util.getIdFromToken(token);
        dto.setSenderId(senderId);
        messageService.sendMessage(dto);
        return ResponseEntity.status(200).body("Message sent successfully!");
    }

    @PostMapping ("/get_msgs")
    @Operation(summary = "Получить сообщения пользователя другому пользователю")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         content = @Content(mediaType = "application/json",
                         array = @ArraySchema(schema = @Schema(implementation = MessageDTO.class)))) })
    public ResponseEntity<Page<MessageDTO>> getMessages(@Validated @RequestBody GetMessageDTO dto,
                                                        @RequestHeader(name = "Authorization") String token,
                                                        @RequestParam(name = "page", defaultValue = "0") int page,
                                                        @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Integer senderId = util.getIdFromToken(token);
        return ResponseEntity.ok(messageService.getMessages(senderId, dto.getRecipientId(), pageable));
    }

    @PostMapping ("/correspondence")
    @Operation(summary = "Получить всю переписку между двумя пользователями")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         content = @Content(mediaType = "application/json",
                         array = @ArraySchema(schema = @Schema(implementation = MessageDTO.class)))) })
    public ResponseEntity<Page<MessageDTO>> getCorrespondence(@Validated @RequestBody GetMessageDTO dto,
                                                              @RequestHeader(name = "Authorization") String token,
                                                              @RequestParam(name = "page", defaultValue = "0") int page,
                                                              @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Integer senderId = util.getIdFromToken(token);
        return ResponseEntity.ok(messageService.getCorrespondence(senderId, dto.getRecipientId(), pageable));
    }
}
