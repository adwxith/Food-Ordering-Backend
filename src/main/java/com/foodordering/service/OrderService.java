package com.foodordering.service;

import com.foodordering.dto.request.OrderRequest;
import com.foodordering.dto.response.OrderResponse;
import com.foodordering.util.Cart;
import java.util.List;

public interface OrderService {
    OrderResponse createOrder(OrderRequest orderRequest, Long userId);
    OrderResponse getOrderById(Long orderId, Long userId);
    List<OrderResponse> getOrdersByUser(Long userId);
    OrderResponse updateOrderStatus(Long orderId, String status);
    void cancelOrder(Long orderId, Long userId);
    List<OrderResponse> getAllOrders();

}