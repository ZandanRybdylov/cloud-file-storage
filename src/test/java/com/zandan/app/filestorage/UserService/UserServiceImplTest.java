package com.zandan.app.filestorage.UserService;


import com.zandan.app.filestorage.dto.UserRequestDto;
import com.zandan.app.filestorage.dto.UserResponseDto;
import com.zandan.app.filestorage.exception.IncorrectLoginOrPasswordException;
import com.zandan.app.filestorage.exception.UserAlreadyExistsException;
import com.zandan.app.filestorage.model.User;
import com.zandan.app.filestorage.repository.UserRepository;
import com.zandan.app.filestorage.service.impl.SessionServiceImpl;
import com.zandan.app.filestorage.service.impl.UserServiceImpl;
import com.zandan.app.filestorage.util.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private SessionServiceImpl sessionServiceImpl;
    @InjectMocks
    private UserServiceImpl userServiceImpl;

    @Test
    public void register_RequestIsValid_ShouldReturnUserResponseDto() {
        //given
        UserRequestDto requestDto = new UserRequestDto("newUser", "password");
        User user = new User(null, "newUser", "password", null);
        User savedUser = new User(1, "newUser", "password", null);
        UserResponseDto responseDto = new UserResponseDto("newUser");

        when(userRepository.findByLogin(requestDto.username())).thenReturn(Optional.empty());
        when(userMapper.toUser(requestDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(savedUser);
        when(userMapper.toUserResponseDto(savedUser)).thenReturn(responseDto);

        //when
        UserResponseDto result = userServiceImpl.register(requestDto);

        //then
        assertThat(result).isEqualTo(responseDto);
        verify(userRepository).findByLogin(requestDto.username());
        verify(userMapper).toUser(requestDto);
        verify(userRepository).save(user);
        verify(userMapper).toUserResponseDto(savedUser);
    }

    @Test
    public void register_UserExists_ShouldThrowUserAlreadyExistsException() {
        //given
        UserRequestDto requestDto = new UserRequestDto("newUser", "password");
        User existingUser = new User(1, "newUser", "password", null);

        when(userRepository.findByLogin(requestDto.username())).thenReturn(Optional.of(existingUser));
        //when
        assertThatThrownBy(() -> userServiceImpl.register(requestDto)
        //then
            ).isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with this username is already registered");
    }

    @Test
    public void authorize_RequestIsValid_ShouldReturnUserResponseDto() {
        //given
        UserRequestDto requestDto = new UserRequestDto("user", "password");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        User user = new User(1, "user", "password", null);
        UserResponseDto responseDto = new UserResponseDto("user");

        when(userRepository.findByLogin(requestDto.username())).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDto(user)).thenReturn(responseDto);
        //when
        var result = userServiceImpl.authorize(requestDto, request);
        //then
        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    public void authorize_InvalidLogin_ShouldThrowIncorrectLoginOrPasswordException() {
        //given
        UserRequestDto requestDto = new UserRequestDto("user", "password");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);

        when(userRepository.findByLogin(requestDto.username())).thenReturn(Optional.empty());
        //when
        assertThatThrownBy(() -> userServiceImpl.authorize(requestDto, request)
        //then
                ).isInstanceOf(IncorrectLoginOrPasswordException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    public void authorize_InvalidPassword_ShouldThrowIncorrectLoginOrPasswordException() {
        UserRequestDto requestDto = new UserRequestDto("user", "invalidPassword");
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        User user = new User(1, "user", "password", null);

        when(userRepository.findByLogin(requestDto.username())).thenReturn(Optional.of(user));
        //when
        assertThatThrownBy(() -> userServiceImpl.authorize(requestDto, request)
        //then
                ).isInstanceOf(IncorrectLoginOrPasswordException.class)
                .hasMessageContaining("Wrong password");
    }
}
