package com.foodordering.service;

import com.foodordering.dto.request.LoginRequest;
import com.foodordering.dto.request.RegisterRequest;
import com.foodordering.dto.response.JwtAuthenticationResponse;
import com.foodordering.exception.AppException;
import com.foodordering.model.Role;
import com.foodordering.model.User;
import com.foodordering.repository.RoleRepository;
import com.foodordering.repository.UserRepository;
import com.foodordering.security.JwtTokenProvider;
import com.foodordering.service.impl.AuthServiceImpl;
import com.foodordering.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RoleRepository roleRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;
    
    @Mock
    private JwtTokenProvider tokenProvider;
    
    @InjectMocks
    private AuthServiceImpl authService;
    
    private LoginRequest loginRequest;
    private RegisterRequest registerRequest;
    private User testUser;
    private Role customerRole;

    @BeforeEach
    void setUp() {
        // Clear security context
        SecurityContextHolder.clearContext();
        
        // Setup test requests
        loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");
        
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setName("Test User");
        registerRequest.setPhone("1234567890");
        registerRequest.setAddress("123 Test St");
        
        // Setup test entities
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        
        customerRole = new Role();
        customerRole.setName(AppConstants.RoleName.ROLE_CUSTOMER);
    }

    @Test
    void authenticateUser_ShouldReturnJwtToken_WhenCredentialsValid() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(tokenProvider.generateToken(authentication)).thenReturn("testToken123");
        
        // Act
        JwtAuthenticationResponse response = authService.authenticateUser(loginRequest);
        
        // Assert
        assertNotNull(response);
        assertEquals("testToken123", response.getAccessToken());
        assertEquals("Bearer", response.getTokenType());
        
        // Verify authentication was set in security context
        assertSame(authentication, SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void registerUser_ShouldRegisterNewUser_WhenUsernameAndEmailAvailable() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(AppConstants.RoleName.ROLE_CUSTOMER))
            .thenReturn(Optional.of(customerRole));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        
        // Act
        Long userId = authService.registerUser(registerRequest);
        
        // Assert
        assertEquals(1L, userId);
        
        // Verify repository interactions
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(roleRepository).findByName(AppConstants.RoleName.ROLE_CUSTOMER);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_ShouldThrowException_WhenUsernameTaken() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(true);
        
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            authService.registerUser(registerRequest);
        });
        
        assertEquals("Username is already taken!", exception.getMessage());
    }

    @Test
    void registerUser_ShouldThrowException_WhenEmailTaken() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(true);
        
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            authService.registerUser(registerRequest);
        });
        
        assertEquals("Email Address already in use!", exception.getMessage());
    }

    @Test
    void registerUser_ShouldThrowException_WhenRoleNotFound() {
        // Arrange
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName(AppConstants.RoleName.ROLE_CUSTOMER))
            .thenReturn(Optional.empty());
        
        // Act & Assert
        AppException exception = assertThrows(AppException.class, () -> {
            authService.registerUser(registerRequest);
        });
        
        assertEquals("User Role not set.", exception.getMessage());
    }
}