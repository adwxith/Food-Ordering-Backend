package com.foodordering.controller.api;

import com.foodordering.dto.request.OrderRequest;
import com.foodordering.dto.request.OrderStatusUpdateRequest;
import com.foodordering.dto.request.UserFeedbackRequest;
import com.foodordering.dto.response.ApiResponse;
import com.foodordering.dto.response.OrderResponse;
import com.foodordering.dto.response.UserFeedbackResponse;
import com.foodordering.security.CurrentUser;
import com.foodordering.security.UserPrincipal;
import com.foodordering.service.OrderService;
import com.foodordering.service.UserFeedbackService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> createOrder(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody OrderRequest orderRequest) {
        logger.info("Creating order for user ID: {}", currentUser.getId());
        OrderResponse response = orderService.createOrder(orderRequest, currentUser.getId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<OrderResponse> getOrder(@CurrentUser UserPrincipal currentUser, @PathVariable Long orderId) {
        logger.info("Getting order ID: {} for user ID: {}", orderId, currentUser.getId());
        OrderResponse response = orderService.getOrderById(orderId, currentUser.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<OrderResponse>> getUserOrders(@CurrentUser UserPrincipal currentUser) {
        logger.info("Getting all orders for user ID: {}", currentUser.getId());
        return ResponseEntity.ok(orderService.getOrdersByUser(currentUser.getId()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_STAFF')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        logger.info("Getting all orders (admin/staff)");
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PutMapping("/{orderId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_STAFF')")
    public ResponseEntity<OrderResponse> updateOrderStatus(@PathVariable Long orderId, @RequestBody @Valid OrderStatusUpdateRequest request) {
        logger.info("Updating order status. ID: {}, New Status: {}", orderId, request.getStatus());
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, request.getStatus()));
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> cancelOrder(@CurrentUser UserPrincipal currentUser, @PathVariable Long orderId) {
        logger.info("Cancelling order ID: {} for user ID: {}", orderId, currentUser.getId());
        orderService.cancelOrder(orderId, currentUser.getId());
        return ResponseEntity.ok(new ApiResponse(true, "Order cancelled successfully"));
    }
    @Autowired
    private UserFeedbackService feedbackService;

    @PostMapping("/feedback/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse> submitFeedback(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long orderId,
            @RequestBody @Valid UserFeedbackRequest request) {
        feedbackService.submitFeedback(orderId, currentUser.getId(), request);
        return ResponseEntity.ok(new ApiResponse(true, "Feedback submitted successfully"));
    }

    @GetMapping("/feedback/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_STAFF', 'CUSTOMER')")
    public ResponseEntity<UserFeedbackResponse> getFeedback(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long orderId) {
        UserFeedbackResponse response = feedbackService.getFeedback(orderId, currentUser.getId());
        return ResponseEntity.ok(response);
    }

}
