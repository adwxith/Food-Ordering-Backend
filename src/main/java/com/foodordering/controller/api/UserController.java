package com.foodordering.controller.api;

import com.foodordering.dto.request.UpdateUserRequest;
import com.foodordering.dto.response.ApiResponse;
import com.foodordering.dto.response.UserProfile;
import com.foodordering.security.CurrentUser;
import com.foodordering.security.UserPrincipal;
import com.foodordering.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<UserProfile> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        logger.info("Fetching profile for user ID: {}", currentUser.getId());
        return ResponseEntity.ok(userService.getUserProfile(currentUser.getId()));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<UserProfile> updateCurrentUser(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody UpdateUserRequest updateRequest) {
        logger.info("Updating profile for user ID: {}", currentUser.getId());
        return ResponseEntity.ok(userService.updateUser(currentUser.getId(), updateRequest));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> deleteCurrentUser(@CurrentUser UserPrincipal currentUser) {
        logger.info("Deleting user ID: {}", currentUser.getId());
        userService.deleteUser(currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "User deleted successfully"));
    }
}
