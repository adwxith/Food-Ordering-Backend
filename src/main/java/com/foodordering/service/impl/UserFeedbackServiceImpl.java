package com.foodordering.service.impl;

import com.foodordering.dto.request.UserFeedbackRequest;
import com.foodordering.dto.response.UserFeedbackResponse;
import com.foodordering.exception.ResourceNotFoundException;
import com.foodordering.exception.AppException;
import com.foodordering.model.Order;
import com.foodordering.model.User;
import com.foodordering.model.UserFeedback;
import com.foodordering.repository.OrderRepository;
import com.foodordering.repository.UserFeedbackRepository;
import com.foodordering.repository.UserRepository;
import com.foodordering.service.UserFeedbackService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserFeedbackServiceImpl implements UserFeedbackService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final UserFeedbackRepository feedbackRepository;

    public UserFeedbackServiceImpl(UserRepository userRepository, OrderRepository orderRepository, UserFeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Override
    @Transactional
    public void submitFeedback(Long orderId, Long userId, UserFeedbackRequest request) {
        if (feedbackRepository.existsByOrderId(orderId)) {
            throw new AppException("Feedback already submitted for this order.");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (!order.getUser().getId().equals(userId)) {
            throw new AppException("You can only give feedback for your own orders.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        UserFeedback feedback = new UserFeedback();
        feedback.setOrder(order);
        feedback.setUser(user);
        feedback.setRating(request.getRating());
        feedback.setComments(request.getComments());
        feedback.setCreatedAt(LocalDateTime.now());

        feedbackRepository.save(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public UserFeedbackResponse getFeedback(Long orderId, Long userId) {
        UserFeedback feedback = feedbackRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Feedback", "orderId", orderId));

        if (!feedback.getUser().getId().equals(userId)) {
            throw new AppException("You can only view feedback for your own orders.");
        }

        return new UserFeedbackResponse(
                feedback.getRating(),
                feedback.getComments(),
                feedback.getCreatedAt()
        );
    }
}
