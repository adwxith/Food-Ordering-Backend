package com.foodordering.controller;

import com.foodordering.dto.request.OrderRequest;
import com.foodordering.dto.response.OrderResponse;
import com.foodordering.exception.ResourceNotFoundException;
import com.foodordering.model.MenuItem;
import com.foodordering.model.Order;
import com.foodordering.model.User;
import com.foodordering.repository.MenuItemRepository;
import com.foodordering.repository.OrderRepository;
import com.foodordering.service.OrderService;
import com.foodordering.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private MenuItemRepository menuItemRepository;

    @InjectMocks
    private OrderService orderService; // Inject mocks into real OrderService

    private OrderRequest orderRequest;
    private User user;
    private MenuItem menuItem1;
    private Order order;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        menuItem1 = new MenuItem();
        menuItem1.setId(1L);
        menuItem1.setName("Pizza");
        menuItem1.setPrice(BigDecimal.valueOf(10.99));

        orderRequest = new OrderRequest();
        orderRequest.setDeliveryAddress("123 Main St");

        OrderRequest.Item item1 = new OrderRequest.Item();
        item1.setMenuItemId(1L);
        item1.setQuantity(2);
        orderRequest.setItems(List.of(item1));

        order = new Order();
        order.setId(1L);
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(AppConstants.OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(21.98)); // 10.99 * 2
    }

    private void mockSecurityContext(String username, String role) {
        SecurityContext securityContext = mock(SecurityContext.class);
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                username, "", List.of(new SimpleGrantedAuthority(role))
        );
        when(securityContext.getAuthentication()).thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(securityContext.getAuthentication().getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createOrder_ShouldCreateNewOrder_WithUserRole() {
        mockSecurityContext("testuser", "ROLE_USER");

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(menuItem1));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponse response = orderService.createOrder(orderRequest, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("PENDING", response.getStatus());
        assertEquals(BigDecimal.valueOf(21.98), response.getTotalAmount());

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAllOrders_ShouldReturnOrders_ForAdminRole() {
        mockSecurityContext("admin", "ROLE_ADMIN");

        Order anotherOrder = new Order();
        anotherOrder.setId(2L);
        anotherOrder.setOrderDate(LocalDateTime.now().minusDays(1));

        when(orderRepository.findAllByOrderByOrderDateDesc()).thenReturn(List.of(order, anotherOrder));

        List<OrderResponse> responses = orderService.getAllOrders();

        assertEquals(2, responses.size());
        assertEquals(1L, responses.get(0).getId());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void createOrder_ShouldThrowExceptionWhenMenuItemNotFound() {
        mockSecurityContext("testuser", "ROLE_USER");

        when(menuItemRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(orderRequest, 1L);
        });
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void updateOrderStatus_ShouldAllowAdminToUpdateStatus() {
        mockSecurityContext("admin", "ROLE_ADMIN");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order updated = invocation.getArgument(0);
            updated.setStatus(AppConstants.OrderStatus.DELIVERED);
            return updated;
        });

        OrderResponse response = orderService.updateOrderStatus(1L, "DELIVERED");

        assertNotNull(response);
        assertEquals("DELIVERED", response.getStatus());
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    void updateOrderStatus_ShouldDenyUserFromUpdatingStatus() {
        mockSecurityContext("testuser", "ROLE_USER");

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(SecurityException.class, () -> {
            orderService.updateOrderStatus(1L, "DELIVERED");
        });
    }
}
