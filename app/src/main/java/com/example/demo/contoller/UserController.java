package com.example.demo.contoller;

import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.dto.RequestTokenRefreshDTO;
import com.example.demo.dto.ResponseTokenRefreshDTO;
import com.example.demo.dto.UserProfileDTO;
import com.example.demo.model.User;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.JwtTokenProvider;
import com.example.demo.service.RefreshTokenService;
import com.example.demo.service.UserService;
import com.example.dto.TwoFactorCodeDTO;
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
    private final RefreshTokenService refreshTokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Зарегистрироваться")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         content = {@Content(mediaType = "application/json",
                         schema = @Schema(implementation = ResponseTokenRefreshDTO.class))})})
    @Tag(name = "public")
    public ResponseEntity<?> register(@Validated @RequestBody RegisterDTO registerDTO) {
        return ResponseEntity.status(200).body(authenticationService.signUp(registerDTO));
    }

    @PostMapping("/login")
    @Operation(summary = "Вход в систему")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         content = {@Content(mediaType = "application/json")}),
            @ApiResponse(responseCode = "404", description = "User not found!")})
    @Tag(name = "public")
    public ResponseEntity<?> login(@Validated @RequestBody LoginDTO loginDTO) {
        return ResponseEntity.ok(authenticationService.signIn(loginDTO));
    }

    @PostMapping("/twoFactor")
    @Operation(summary = "Двухфакторная аутентификация")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTokenRefreshDTO.class))}),
            @ApiResponse(responseCode = "401", description = "Email microservice returned non ok status!"),
            @ApiResponse(responseCode = "503", description = "Microservice email_service is unavailable now!")})
    @Tag(name = "public")
    public ResponseEntity<?> twoFactorAuthentication(@Validated @RequestBody TwoFactorCodeDTO dto) {
        return ResponseEntity.ok(authenticationService.twoFactorAuthentication(dto));
    }

    @PutMapping("/profile/update")
    @Operation(summary = "Обновить профиль")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                         content = {@Content(mediaType = "application/json",
                         schema = @Schema(implementation = User.class))}),
            @ApiResponse(responseCode = "404", description = "User not found!")})
    public ResponseEntity<User> update(@RequestBody UserProfileDTO dto, Principal principal) {
        return ResponseEntity.ok(userService.updateUserProfile(principal.getName(), dto));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Обновить токен пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    content = {@Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseTokenRefreshDTO.class))}),
            @ApiResponse(responseCode = "404", description = "Refresh token not found!"),
            @ApiResponse(responseCode = "403", description = "Refresh token was expired!")})
    public ResponseEntity<?> refresh(@Validated @RequestBody RequestTokenRefreshDTO dto) {
        String refreshToken = dto.getToken();
        var token = refreshTokenService.findByRefreshToken(refreshToken);

        refreshTokenService.verifyExpiration(token);

        return ResponseEntity.ok(ResponseTokenRefreshDTO.builder()
                .accessToken(jwtTokenProvider.generateToken(token.getUser()))
                .refreshToken(refreshToken)
                .build());
    }
}
