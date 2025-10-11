package com.zandan.app.filestorage.controller;

import com.zandan.app.filestorage.dto.UserRequestDto;
import com.zandan.app.filestorage.dto.UserResponseDto;
import com.zandan.app.filestorage.service.impl.UserServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserServiceImpl userServiceImpl;

    @Operation(
            summary = "Зарегистрироваться",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Пользователь зарегистрирован в системе"),
                    @ApiResponse(responseCode = "400", description = "Невалидный логин или пароль"),
                    @ApiResponse(responseCode = "409", description = "Пользователь с таким логином уже существует")

            }
    )
    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDto> registerUser(@RequestBody @Valid UserRequestDto userRequestDto,
                                                        BindingResult bindingResult) throws BindException {
        if (bindingResult.hasErrors()) {
            throw new BindException(bindingResult);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(userServiceImpl.register(userRequestDto));
    }

    @Operation(
            summary = "Авторизироваться в системе",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Пользователь успешно авторизирован"),
                    @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
            }
    )
    @PostMapping("/sign-in")
    public ResponseEntity<UserResponseDto> authorizeUser(@RequestBody UserRequestDto userRequestDto,
                                                         HttpServletRequest request) {
        return ResponseEntity.ok().body(userServiceImpl.authorize(userRequestDto, request));
    }

    @Operation(
            summary = "Логаут из системы",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Логаут прошел успешно"),
                    @ApiResponse(responseCode = "401", description = "Операция выполняется неавторизованным пользователем")
            }
    )
    @PostMapping("/sign-out")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }
}
