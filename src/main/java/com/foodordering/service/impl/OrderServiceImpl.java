package com.foodordering.service.impl;

import com.foodordering.dto.request.OrderRequest;
import com.foodordering.dto.response.*;
import com.foodordering.exception.*;
import com.foodordering.model.*;
import com.foodordering.repository.*;
import com.foodordering.service.OrderService;
import com.foodordering.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MenuItemRepository menuItemRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            UserRepository userRepository,
                            MenuItemRepository menuItemRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    @Transactional
    public OrderResponse createOrder(OrderRequest orderRequest, Long userId) {
        logger.info("Creating order for userId: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id {}", userId);
                    return new ResourceNotFoundException("User", "id", userId);
                });

        Order order = new Order();
        order.setUser(user);
        order.setDeliveryAddress(orderRequest.getDeliveryAddress());
        order.setRestaurantNotes(orderRequest.getRestaurantNotes());
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(AppConstants.OrderStatus.PENDING);
        order.setPaymentStatus(AppConstants.PaymentStatus.PENDING);

        for (OrderRequest.Item itemRequest : orderRequest.getItems()) {
            logger.debug("Processing item with menuItemId: {}", itemRequest.getMenuItemId());
            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> {
                        logger.error("MenuItem not found with id {}", itemRequest.getMenuItemId());
                        return new ResourceNotFoundException("MenuItem", "id", itemRequest.getMenuItemId());
                    });

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menuItem);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPriceAtOrderTime(menuItem.getPrice());

            order.getItems().add(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        logger.info("Order created successfully with id: {}", savedOrder.getId());
        return convertToDto(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId, Long userId) {
        logger.info("Fetching order with id {} for userId {}", orderId, userId);
        Order order = findOrderByIdAndUserId(orderId, userId);
        return convertToDto(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUser(Long userId) {
        logger.info("Fetching all orders for userId: {}", userId);
        User user = findUserById(userId);
        return orderRepository.findByUser(user).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        logger.info("Updating order status for orderId {} to '{}'", orderId, status);
        Order order = findOrderById(orderId);
        updateOrderStatus(order, status);
        Order updatedOrder = orderRepository.save(order);
        logger.info("Order status updated successfully for orderId {}", orderId);
        return convertToDto(updatedOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        logger.info("Cancelling order with id {} for userId {}", orderId, userId);
        Order order = findOrderByIdAndUserId(orderId, userId);
        validateOrderCancellation(order);
        order.setStatus(AppConstants.OrderStatus.CANCELLED);
        orderRepository.save(order);
        logger.info("Order cancelled successfully with id {}", orderId);
    }

    private User findUserByUsername(String username) {
        logger.debug("Fetching user by username: {}", username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.error("User not found with username {}", username);
                    return new ResourceNotFoundException("User", "username", username);
                });
    }

    private User findUserById(Long userId) {
        logger.debug("Fetching user by id: {}", userId);
        return userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found with id {}", userId);
                    return new ResourceNotFoundException("User", "id", userId);
                });
    }

    private Order findOrderById(Long orderId) {
        logger.debug("Fetching order by id: {}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    logger.error("Order not found with id {}", orderId);
                    return new ResourceNotFoundException("Order", "id", orderId);
                });
    }

    private Order findOrderByIdAndUserId(Long orderId, Long userId) {
        logger.debug("Fetching order with id {} for userId {}", orderId, userId);
        return orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> {
                    logger.error("Order not found with id {} and userId {}", orderId, userId);
                    return new ResourceNotFoundException("Order", "id", orderId);
                });
    }

    private void updateOrderStatus(Order order, String status) {
        try {
            AppConstants.OrderStatus newStatus = AppConstants.OrderStatus.valueOf(status.toUpperCase());
            order.setStatus(newStatus);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid order status: {}", status);
            throw new InvalidStatusException("Invalid order status: " + status);
        }
    }

    private void validateOrderCancellation(Order order) {
        if (order.getStatus() != AppConstants.OrderStatus.PENDING) {
            logger.warn("Attempted to cancel a non-pending order with id {}", order.getId());
            throw new OrderProcessingException("Order cannot be cancelled as it's already being processed");
        }
    }

    private OrderResponse convertToDto(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getUsername(),
                order.getOrderDate(),
                order.getDeliveryAddress(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getPaymentStatus().name(),
                convertOrderItemsToDto(order.getItems()),
                order.getRestaurantNotes()
        );
    }

    private List<OrderItemResponse> convertOrderItemsToDto(Set<OrderItem> items) {
        return items.stream()
                .map(this::convertOrderItemToDto)
                .collect(Collectors.toList());
    }

    private OrderItemResponse convertOrderItemToDto(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getMenuItem().getId(),
                item.getMenuItem().getName(),
                item.getQuantity(),
                item.getPriceAtOrderTime(),
                calculateItemTotal(item.getMenuItem(), item.getQuantity())
        );
    }

    private BigDecimal calculateItemTotal(MenuItem menuItem, int quantity) {
        return menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        logger.info("Fetching all orders");
        List<Order> orders = orderRepository.findAllByOrderByOrderDateDesc();
        return orders.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
