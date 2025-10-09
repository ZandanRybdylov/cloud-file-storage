package com.zandan.app.filestorage.util;


import com.zandan.app.filestorage.model.User;
import com.zandan.app.filestorage.model.UserRole;
import com.zandan.app.filestorage.dto.UserRequestDto;
import com.zandan.app.filestorage.dto.UserResponseDto;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto toUserResponseDto(User user) {
        return new UserResponseDto(user.getLogin());
    }

    public User toUser(UserRequestDto userRequestDto) {
        return new User(null, userRequestDto.username(), userRequestDto.password(), UserRole.ROLE_USER);
    }
}
