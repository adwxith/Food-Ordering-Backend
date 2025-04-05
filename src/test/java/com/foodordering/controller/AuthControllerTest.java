package com.foodordering.controller;

import com.foodordering.controller.api.AuthController;
import com.foodordering.dto.request.LoginRequest;
import com.foodordering.dto.request.RegisterRequest;
import com.foodordering.dto.response.ApiResponse;
import com.foodordering.dto.response.JwtAuthenticationResponse;
import com.foodordering.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class AuthControllerTest {

    private AuthController authController;
    private TestAuthService authService;
    
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        authService = new TestAuthService();
        authController = new AuthController(authService);
        
        loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("restaurant@foodordering.com");
        loginRequest.setPassword("restaurant123");
        
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setName("Test User");
        registerRequest.setPhone("1234567890");
        registerRequest.setAddress("123 Test St");
    }

    @Test
    void login_ShouldReturnJwtResponse() {
        // Setup test service behavior
        authService.setMockToken("testToken123");
        
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof JwtAuthenticationResponse);
        
        JwtAuthenticationResponse jwtResponse = (JwtAuthenticationResponse) response.getBody();
        assertEquals("testToken123", jwtResponse.getAccessToken());
        assertEquals("Bearer", jwtResponse.getTokenType());
        
        // Verify the service received the correct request
        assertEquals(loginRequest, authService.getLastLoginRequest());
    }

    @Test
    void register_ShouldReturnSuccessResponse() {
        // Setup test service behavior
        authService.setMockUserId(42L);
        
        ResponseEntity<?> response = authController.registerUser(registerRequest);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof ApiResponse);
        
        ApiResponse apiResponse = (ApiResponse) response.getBody();
        assertTrue(apiResponse.getSuccess());
        assertEquals("User registered successfully", apiResponse.getMessage());
        assertEquals(42L, apiResponse.getData());
        
        // Verify the service received the correct request
        assertEquals(registerRequest, authService.getLastRegisterRequest());
    }

    // Test implementation of AuthService
    private static class TestAuthService implements AuthService {
        private String mockToken;
        private Long mockUserId;
        private LoginRequest lastLoginRequest;
        private RegisterRequest lastRegisterRequest;

        @Override
        public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
            this.lastLoginRequest = loginRequest;
            return new JwtAuthenticationResponse(mockToken);
        }

        @Override
        public Long registerUser(RegisterRequest registerRequest) {
            this.lastRegisterRequest = registerRequest;
            return mockUserId;
        }

        public void setMockToken(String token) {
            this.mockToken = token;
        }

        public void setMockUserId(Long userId) {
            this.mockUserId = userId;
        }

        public LoginRequest getLastLoginRequest() {
            return lastLoginRequest;
        }

        public RegisterRequest getLastRegisterRequest() {
            return lastRegisterRequest;
        }
    }
}