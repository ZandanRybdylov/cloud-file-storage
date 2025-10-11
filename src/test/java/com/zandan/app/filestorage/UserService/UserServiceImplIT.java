package com.zandan.app.filestorage.UserService;

import com.zandan.app.filestorage.AbstractIntegrationTest;
import com.zandan.app.filestorage.dto.UserRequestDto;
import com.zandan.app.filestorage.dto.UserResponseDto;
import com.zandan.app.filestorage.exception.IncorrectLoginOrPasswordException;
import com.zandan.app.filestorage.exception.UserAlreadyExistsException;
import com.zandan.app.filestorage.repository.UserRepository;
import com.zandan.app.filestorage.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class UserServiceImplIT extends AbstractIntegrationTest {

    @Autowired
    private UserServiceImpl userServiceImpl;
    @Autowired
    private UserRepository userRepository;

    @Test
    @Transactional
    public void register_ValidUser_ShouldSaveToDatabase() {
        UserRequestDto requestDto = new UserRequestDto("username", "password");
        UserResponseDto response = userServiceImpl.register(requestDto);

        assertThat(response.username()).isEqualTo(requestDto.username());
        assertThat(userRepository.findByLogin(requestDto.username())).isPresent();
    }

    @Test
    @Transactional
    public void register_UserExists_ShouldThrowUserAlreadyExistsException() {
        UserRequestDto requestDto = new UserRequestDto("username", "password");

        var user = userServiceImpl.register(requestDto);
        assertThatThrownBy(() -> userServiceImpl.register(requestDto))
                .isInstanceOf(UserAlreadyExistsException.class)
                .hasMessageContaining("User with this username is already registered");
    }

    @Test
    @Transactional
    public void authorize_ValidUser_ShouldReturnUserResponseDto() {
        UserRequestDto requestDto = new UserRequestDto("username", "password");
        MockHttpServletRequest request = new MockHttpServletRequest();
        userServiceImpl.register(requestDto);

        UserResponseDto response = userServiceImpl.authorize(requestDto, request);
        assertThat(response.username()).isEqualTo(requestDto.username());
        assertThat(request.getSession(false)).isNotNull();
    }

    @Test
    public void authorize_UserNotExist_ShouldThrowIncorrectLoginOrPasswordException() {
        UserRequestDto requestDto = new UserRequestDto("username", "password");
        MockHttpServletRequest request = new MockHttpServletRequest();

        assertThatThrownBy(() -> userServiceImpl.authorize(requestDto, request))
                .isInstanceOf(IncorrectLoginOrPasswordException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    @Transactional
    public void authorize_WrongPassword_ShouldThrowIncorrectLoginOrPasswordException() {
        UserRequestDto requestDto = new UserRequestDto("username", "password");
        UserRequestDto requestDto2 = new UserRequestDto("username", "password2");
        MockHttpServletRequest request = new MockHttpServletRequest();
        userServiceImpl.register(requestDto2);

        assertThatThrownBy(() -> userServiceImpl.authorize(requestDto, request))
                .isInstanceOf(IncorrectLoginOrPasswordException.class)
                .hasMessageContaining("Wrong password");
    }
}
