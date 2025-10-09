package com.zandan.app.filestorage.controller;

import com.zandan.app.filestorage.dto.UserResponseDto;
import com.zandan.app.filestorage.security.MyUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserController {

    @Operation(summary = "Узнать текущего пользователя",
                responses = {
                        @ApiResponse(responseCode = "200", description = "Текущий пользователь"),
                        @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
                })
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getUser(@AuthenticationPrincipal MyUserDetails userDetails) {
        UserResponseDto userResponseDto = new UserResponseDto(userDetails.getUsername());
        return ResponseEntity.ok().body(userResponseDto);
    }
}
