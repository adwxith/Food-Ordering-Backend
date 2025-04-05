package com.foodordering.service.impl;

import com.foodordering.dto.request.UpdateUserRequest;
import com.foodordering.dto.response.UserProfile;
import com.foodordering.exception.ResourceNotFoundException;
import com.foodordering.model.User;
import com.foodordering.repository.UserRepository;
import com.foodordering.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserProfile getUserProfile(Long userId) {
        logger.info("Fetching user profile for User ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User", "id", userId);
                });

        logger.debug("User profile found for User ID: {}", userId);
        return new UserProfile(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getAddress(),
                user.getPhone()
        );
    }

    @Override
    public UserProfile updateUser(Long userId, UpdateUserRequest updateRequest) {
        logger.info("Updating user profile for User ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User", "id", userId);
                });

        if (updateRequest.getName() != null) {
            user.setName(updateRequest.getName());
        }
        if (updateRequest.getEmail() != null) {
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getAddress() != null) {
            user.setAddress(updateRequest.getAddress());
        }
        if (updateRequest.getPhone() != null) {
            user.setPhone(updateRequest.getPhone());
        }

        User updatedUser = userRepository.save(user);
        logger.info("User profile updated for User ID: {}", userId);

        return new UserProfile(
                updatedUser.getId(),
                updatedUser.getUsername(),
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getAddress(),
                updatedUser.getPhone()
        );
    }

    @Override
    public void deleteUser(Long userId) {
        logger.info("Deleting user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with ID: {}", userId);
                    return new ResourceNotFoundException("User", "id", userId);
                });

        userRepository.delete(user);
        logger.info("User deleted with ID: {}", userId);
    }
}
