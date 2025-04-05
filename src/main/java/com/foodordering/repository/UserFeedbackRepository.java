package com.foodordering.repository;

import com.foodordering.model.UserFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFeedbackRepository extends JpaRepository<UserFeedback, Long> {
    Optional<UserFeedback> findByOrderId(Long orderId);
    boolean existsByOrderId(Long orderId);
}
