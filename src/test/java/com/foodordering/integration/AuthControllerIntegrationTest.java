package com.foodordering.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodordering.dto.request.LoginRequest;
import com.foodordering.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerUser_ShouldReturnSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("testuser@example.com");
        request.setPassword("password123");
        request.setName("Test User");
        request.setPhone("9876543210");
        request.setAddress("Test Address");

        ResultActions result = mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.accessToken", notNullValue()))
              .andExpect(jsonPath("$.tokenType", is("Bearer")));
    }

    @Test
    void loginUser_ShouldReturnJwt() throws Exception {
        // Register user first
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("testuser@example.com");
        request.setPassword("password123");
        request.setName("Test User");
        request.setPhone("9876543210");
        request.setAddress("Test Address");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk());

        // Login now
        LoginRequest login = new LoginRequest();
        login.setUsernameOrEmail("testuser@example.com");
        login.setPassword("password123");

        ResultActions result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)));

        result.andExpect(status().isOk())
              .andExpect(jsonPath("$.accessToken", notNullValue()))
              .andExpect(jsonPath("$.tokenType", is("Bearer")));
    }

    @Test
    void loginUser_InvalidCredentials_ShouldReturn401() throws Exception {
        LoginRequest login = new LoginRequest();
        login.setUsernameOrEmail("invalid@example.com");
        login.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void registerUser_WithDuplicateEmail_ShouldReturnError() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("duplicate@example.com");
        request.setPassword("password123");
        request.setName("Test User");
        request.setPhone("9876543210");
        request.setAddress("Test Address");

        // First time - should pass
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isOk());

        // Second time - should fail
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.message", containsString("already exists")));
    }
}
