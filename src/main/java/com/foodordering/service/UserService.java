
package com.foodordering.service;

import com.foodordering.dto.response.UserProfile;
import com.foodordering.dto.request.UpdateUserRequest;
import com.foodordering.model.User;

public interface UserService {
    UserProfile getUserProfile(Long userId);
    UserProfile updateUser(Long userId, UpdateUserRequest updateRequest);
    void deleteUser(Long userId);
}