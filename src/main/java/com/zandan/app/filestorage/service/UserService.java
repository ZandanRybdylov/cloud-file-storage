package com.zandan.app.filestorage.service;

import com.zandan.app.filestorage.exception.IncorrectLoginOrPasswordException;
import com.zandan.app.filestorage.model.User;
import com.zandan.app.filestorage.dto.UserRequestDto;
import com.zandan.app.filestorage.dto.UserResponseDto;
import com.zandan.app.filestorage.exception.UserAlreadyExistsException;
import com.zandan.app.filestorage.repository.UserRepository;
import com.zandan.app.filestorage.util.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper = new UserMapper();
    private final SessionService sessionService;

    public UserResponseDto register(UserRequestDto userRequestDto) {
        if (userRepository.findByLogin(userRequestDto.username()).isPresent()) {
            throw new UserAlreadyExistsException("User with this username is already registered");
        }
        User user = userMapper.toUser(userRequestDto);
        user = userRepository.save(user);
        return userMapper.toUserResponseDto(user);
    }

    public UserResponseDto authorize(UserRequestDto userRequestDto, HttpServletRequest request) {
        User user = userRepository.findByLogin(userRequestDto.username())
                .orElseThrow(() -> new IncorrectLoginOrPasswordException("User not found"));
        if (!user.getPassword().equals(userRequestDto.password())) {
            throw new IncorrectLoginOrPasswordException("Wrong password");
        }

        sessionService.setSessionAttribute(user, request);
        return userMapper.toUserResponseDto(user);
    }
}
