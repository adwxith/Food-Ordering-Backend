package com.foodordering.service;

import com.foodordering.dto.request.UserFeedbackRequest;
import com.foodordering.dto.response.UserFeedbackResponse;

public interface UserFeedbackService {
    void submitFeedback(Long orderId, Long userId, UserFeedbackRequest request);
    UserFeedbackResponse getFeedback(Long orderId, Long userId);
}
