package com.example.demo.contoller;

import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.dto.UserProfileDTO;
import com.example.demo.model.JwtAuthenticationResponse;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;

import java.security.Principal;

@RestController
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Зарегистрироваться")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                         content = {@Content(mediaType = "application/json",
                         schema = @Schema(implementation = JwtAuthenticationResponse.class))})})
    @Tag(name = "public")
    public ResponseEntity<JwtAuthenticationResponse> register(@Validated @RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.status(201).body(userService.signUp(registerDTO));
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         content = {@Content(mediaType = "application/json",
                         schema = @Schema(implementation = JwtAuthenticationResponse.class))}),
            @ApiResponse(responseCode = "404", description = "User not found!")})
    @Tag(name = "public")
    public ResponseEntity<JwtAuthenticationResponse> login(@Validated @RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(userService.signIn(loginDTO));
    }

    @PutMapping("/profile/update")
    @Operation(summary = "Обновить профиль")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         content = {@Content(mediaType = "application/json",
                         schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "404", description = "User not found!")})
    public ResponseEntity<User> update(@RequestBody UserProfileDTO dto, Principal principal) {
        return ResponseEntity.ok(userService.updateProfile(principal.getName(), dto));
    }
}
