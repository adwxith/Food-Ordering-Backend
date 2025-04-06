package com.foodordering.controller;

import com.foodordering.controller.api.OrderController;
import com.foodordering.dto.request.OrderRequest;
import com.foodordering.dto.request.OrderStatusUpdateRequest;
import com.foodordering.dto.response.ApiResponse;
import com.foodordering.dto.response.OrderResponse;
import com.foodordering.security.UserPrincipal;
import com.foodordering.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderControllerServiceTest {

    private OrderController orderController;
    private TestOrderService orderService;
    private UserPrincipal customerUser;
    private UserPrincipal adminUser;
    
    private OrderRequest orderRequest;
    private OrderStatusUpdateRequest statusUpdateRequest;

    @BeforeEach
    void setUp() {
        // Create test service implementation
        orderService = new TestOrderService();
        orderController = new OrderController(orderService);
        
        // Create test users
        customerUser = createUserPrincipal(1L, "customer", "ROLE_CUSTOMER");
        adminUser = createUserPrincipal(2L, "admin", "ROLE_ADMIN");
        
        // Setup test requests
        orderRequest = new OrderRequest();
        orderRequest.setDeliveryAddress("123 Main St");
        
        statusUpdateRequest = new OrderStatusUpdateRequest();
        statusUpdateRequest.setStatus("PREPARING");
    }

    private UserPrincipal createUserPrincipal(Long id, String username, String role) {
        return new UserPrincipal(
            id, 
            username, 
            username + "@example.com", 
            "password", 
            Collections.singletonList(new SimpleGrantedAuthority(role))
        );
    }

    @Test
    void createOrder_ShouldCreateNewOrder_ForCustomer() {
        // Setup
        OrderResponse expectedResponse = new OrderResponse();
        expectedResponse.setId(1L);
        expectedResponse.setStatus("PENDING");
        orderService.setMockResponse(expectedResponse);
        
        // Execute
        ResponseEntity<OrderResponse> response = 
            orderController.createOrder(customerUser, orderRequest);
        
        // Verify
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("PENDING", response.getBody().getStatus());
        assertEquals(customerUser.getId(), orderService.getLastUserId());
    }

    @Test
    void getAllOrders_ShouldReturnOrders_ForAdmin() {
        // Setup
        OrderResponse order = new OrderResponse();
        order.setId(1L);
        orderService.setMockOrdersList(List.of(order));
        
        // Execute
        ResponseEntity<List<OrderResponse>> response = orderController.getAllOrders();
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1L, response.getBody().get(0).getId());
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus_ForAdmin() {
        // Setup
        OrderResponse expectedResponse = new OrderResponse();
        expectedResponse.setStatus("PREPARING");
        orderService.setMockResponse(expectedResponse);
        
        // Execute
        ResponseEntity<OrderResponse> response = 
            orderController.updateOrderStatus(1L, statusUpdateRequest);
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PREPARING", response.getBody().getStatus());
        assertEquals(1L, orderService.getLastOrderId());
    }

    @Test
    void cancelOrder_ShouldCancelOrder_ForCustomer() {
        // Execute
        ResponseEntity<ApiResponse> response = 
            orderController.cancelOrder(customerUser, 1L);
        
        // Verify
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getSuccess());
        assertEquals("Order cancelled successfully", response.getBody().getMessage());
        assertEquals(1L, orderService.getLastOrderId());
        assertEquals(customerUser.getId(), orderService.getLastUserId());
    }

    // Test implementation of OrderService
    private static class TestOrderService implements OrderService {
        private OrderResponse mockResponse;
        private List<OrderResponse> mockOrdersList;
        private Long lastOrderId;
        private Long lastUserId;
        private OrderRequest lastOrderRequest;

        @Override
        public OrderResponse createOrder(OrderRequest orderRequest, Long userId) {
            this.lastOrderRequest = orderRequest;
            this.lastUserId = userId;
            return mockResponse;
        }

        @Override
        public OrderResponse getOrderById(Long orderId, Long userId) {
            this.lastOrderId = orderId;
            this.lastUserId = userId;
            return mockResponse;
        }

        @Override
        public List<OrderResponse> getOrdersByUser(Long userId) {
            this.lastUserId = userId;
            return mockOrdersList;
        }

        @Override
        public OrderResponse updateOrderStatus(Long orderId, String status) {
            this.lastOrderId = orderId;
            return mockResponse;
        }

        @Override
        public void cancelOrder(Long orderId, Long userId) {
            this.lastOrderId = orderId;
            this.lastUserId = userId;
        }

        @Override
        public List<OrderResponse> getAllOrders() {
            return mockOrdersList;
        }

        public void setMockResponse(OrderResponse response) {
            this.mockResponse = response;
        }

        public void setMockOrdersList(List<OrderResponse> ordersList) {
            this.mockOrdersList = ordersList;
        }

        public Long getLastOrderId() {
            return lastOrderId;
        }

        public Long getLastUserId() {
            return lastUserId;
        }

        public OrderRequest getLastOrderRequest() {
            return lastOrderRequest;
        }
    }
}