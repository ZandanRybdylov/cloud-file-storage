package com.zandan.app.filestorage.service;

import com.zandan.app.filestorage.dto.UserRequestDto;
import com.zandan.app.filestorage.dto.UserResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface UserService {

    UserResponseDto register(UserRequestDto userRequestDto);

    UserResponseDto authorize(UserRequestDto userRequestDto, HttpServletRequest request);
}
