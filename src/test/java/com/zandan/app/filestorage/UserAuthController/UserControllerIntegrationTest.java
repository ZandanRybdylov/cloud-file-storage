package com.zandan.app.filestorage.UserAuthController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zandan.app.filestorage.AbstractIntegrationTest;
import com.zandan.app.filestorage.dto.UserRequestDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class UserControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @Transactional
    public void registerUser_ValidUser_ShouldPersistInPostgres() throws Exception {
        UserRequestDto requestDto = new UserRequestDto("username", "password");

        mockMvc.perform(post("/api/auth/sign-up")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("username"));
    }

    @Test
    public void registerUser_InvalidUsername_ShouldReturnBadRequest() throws Exception {
        UserRequestDto requestDto = new UserRequestDto("user", "password");

        mockMvc.perform(post("/api/auth/sign-up")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @Transactional
    public void authorizeUser_ValidCredentials_ShouldReturnSessionCookie() throws Exception {
        UserRequestDto request = new UserRequestDto("username", "password");

        mockMvc.perform(post("/api/auth/sign-up")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("username"));

        mockMvc.perform(post("/api/auth/sign-in")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("username"));
    }

    @Test
    public void authorizeUser_UserNotExist_ShouldReturnBadRequest() throws Exception {
        UserRequestDto request = new UserRequestDto("username", "password");

        mockMvc.perform(post("/api/auth/sign-in")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
