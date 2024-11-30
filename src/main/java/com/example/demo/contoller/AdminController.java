package com.example.demo.contoller;

import com.example.demo.dto.ChangeRoleDTO;
import com.example.demo.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/changeRole")
    @Operation(summary = "Сменить роль пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User role updated successfully", content = @Content),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content)})
    public ResponseEntity<String> changeUserRole(@Validated @RequestBody ChangeRoleDTO dto) {
        userService.changeUserRole(dto);
        return ResponseEntity.ok("User role updated successfully");
    }
}

