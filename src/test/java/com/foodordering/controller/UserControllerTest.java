package com.foodordering.controller;

import com.foodordering.controller.api.UserController;
import com.foodordering.dto.request.UpdateUserRequest;
import com.foodordering.dto.response.ApiResponse;
import com.foodordering.dto.response.UserProfile;
import com.foodordering.security.UserPrincipal;
import com.foodordering.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController userController;
    private TestUserService userService;
    private UserPrincipal currentUser;
    
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        userService = new TestUserService();
        userController = new UserController(userService);
        
        // Create UserPrincipal directly using constructor
        Long userId = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String password = "password";
        List<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_CUSTOMER")
        );
        
        currentUser = new UserPrincipal(
            userId, 
            username, 
            email, 
            password, 
            authorities
        );
        
        // Setup test request
        updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPhone("9876543210");
        updateRequest.setAddress("456 Updated St");
    }

    @Test
    void getCurrentUser_ShouldReturnUserProfile() {
        // Setup test service behavior
        UserProfile mockProfile = new UserProfile(1L, "testuser", "Test User", 
                "test@example.com", "123 Test St", "1234567890");
        userService.setMockUserProfile(mockProfile);
        
        ResponseEntity<UserProfile> response = userController.getCurrentUser(currentUser);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        UserProfile profile = response.getBody();
        assertEquals(mockProfile, profile);
        assertEquals(1L, profile.getId());
        assertEquals("testuser", profile.getUsername());
        
        // Verify the service received the correct user ID
        assertEquals(currentUser.getId(), userService.getLastUserId());
    }

    @Test
    void updateCurrentUser_ShouldReturnUpdatedProfile() {
        // Setup test service behavior
        UserProfile mockUpdatedProfile = new UserProfile(1L, "testuser", "Updated Name", 
                "updated@example.com", "456 Updated St", "9876543210");
        userService.setMockUserProfile(mockUpdatedProfile);
        
        ResponseEntity<UserProfile> response = userController.updateCurrentUser(currentUser, updateRequest);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        UserProfile updatedProfile = response.getBody();
        assertEquals(mockUpdatedProfile, updatedProfile);
        assertEquals("Updated Name", updatedProfile.getName());
        assertEquals("updated@example.com", updatedProfile.getEmail());
        
        // Verify the service received the correct parameters
        assertEquals(currentUser.getId(), userService.getLastUserId());
        assertEquals(updateRequest, userService.getLastUpdateRequest());
    }

    @Test
    void deleteCurrentUser_ShouldReturnSuccessResponse() {
        ResponseEntity<ApiResponse> response = userController.deleteCurrentUser(currentUser);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ApiResponse apiResponse = response.getBody();
        assertTrue(apiResponse.getSuccess());
        assertEquals("User deleted successfully", apiResponse.getMessage());
        
        // Verify the service received the correct user ID
        assertEquals(currentUser.getId(), userService.getLastDeletedUserId());
    }

    // Test implementation of UserService
    private static class TestUserService implements UserService {
        private UserProfile mockUserProfile;
        private Long lastUserId;
        private UpdateUserRequest lastUpdateRequest;
        private Long lastDeletedUserId;

        @Override
        public UserProfile getUserProfile(Long userId) {
            this.lastUserId = userId;
            return mockUserProfile;
        }

        @Override
        public UserProfile updateUser(Long userId, UpdateUserRequest updateRequest) {
            this.lastUserId = userId;
            this.lastUpdateRequest = updateRequest;
            return mockUserProfile;
        }

        @Override
        public void deleteUser(Long userId) {
            this.lastDeletedUserId = userId;
        }

        public void setMockUserProfile(UserProfile userProfile) {
            this.mockUserProfile = userProfile;
        }

        public Long getLastUserId() {
            return lastUserId;
        }

        public UpdateUserRequest getLastUpdateRequest() {
            return lastUpdateRequest;
        }

        public Long getLastDeletedUserId() {
            return lastDeletedUserId;
        }
    }
}