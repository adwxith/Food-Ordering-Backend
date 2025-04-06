package com.foodordering.controller;

import com.foodordering.controller.api.PaymentController;
import com.foodordering.dto.request.PaymentRequest;
import com.foodordering.dto.response.PaymentResponse;
import com.foodordering.exception.ResourceNotFoundException;
import com.foodordering.security.UserPrincipal;
import com.foodordering.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentControllerTest {

    private PaymentController paymentController;
    private TestPaymentService paymentService;
    private UserPrincipal customerPrincipal;
    private UserPrincipal staffPrincipal;

    @BeforeEach
    void setUp() {
        paymentService = new TestPaymentService();
        paymentController = new PaymentController(paymentService);

        // Setup test users
        customerPrincipal = new UserPrincipal(
            1L, "customer", "customer@example.com", "password",
            List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );

        staffPrincipal = new UserPrincipal(
            2L, "staff", "staff@example.com", "password",
            List.of(new SimpleGrantedAuthority("ROLE_STAFF"))
        );

        setupSecurityContext(customerPrincipal);
    }

    @Test
    void processPayment_ShouldProcessValidPayment() {
        // Given
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1L);
        request.setPaymentMethod("CREDIT_CARD");

        PaymentResponse mockResponse = new PaymentResponse();
        mockResponse.setId(1L);
        mockResponse.setOrderId(1L);
        mockResponse.setStatus("COMPLETED");
        paymentService.setMockResponse(mockResponse);

        // When
        ResponseEntity<PaymentResponse> response = 
            paymentController.processPayment(customerPrincipal, request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getOrderId());
        assertEquals("COMPLETED", response.getBody().getStatus());
    }

    @Test
    void processPayment_ShouldRejectInvalidOrderId() {
        // Given
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(null); // Invalid
        request.setPaymentMethod("CREDIT_CARD");

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            paymentController.processPayment(customerPrincipal, request);
        });
    }

    @Test
    void processPayment_ShouldRejectInvalidPaymentMethod() {
        // Given
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1L);
        request.setPaymentMethod(""); // Invalid

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            paymentController.processPayment(customerPrincipal, request);
        });
    }

    @Test
    void processPayment_ShouldRejectUnauthorizedUser() {
        // Given
        setupSecurityContext(staffPrincipal); // Staff shouldn't be able to process payments
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1L);
        request.setPaymentMethod("CREDIT_CARD");

        // When/Then
        assertThrows(org.springframework.security.access.AccessDeniedException.class, () -> {
            paymentController.processPayment(staffPrincipal, request);
        });
    }

    @Test
    void getPaymentByOrderId_ShouldReturnPayment() {
        // Given
        PaymentResponse mockResponse = new PaymentResponse();
        mockResponse.setId(1L);
        mockResponse.setOrderId(1L);
        mockResponse.setStatus("COMPLETED");
        paymentService.setMockResponse(mockResponse);

        // When
        ResponseEntity<PaymentResponse> response = 
            paymentController.getPaymentByOrderId(customerPrincipal, 1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
    }

    

   

    private void setupSecurityContext(UserPrincipal principal) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal, null, principal.getAuthorities());
        
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    // Test implementation of PaymentService
    private static class TestPaymentService implements PaymentService {
        private PaymentResponse mockResponse;
        private PaymentRequest lastRequest;

        @Override
        public PaymentResponse processPayment(PaymentRequest paymentRequest) {
            // Validate request
            if (paymentRequest.getOrderId() == null) {
                throw new IllegalArgumentException("Order ID is required");
            }
            if (paymentRequest.getPaymentMethod() == null || paymentRequest.getPaymentMethod().isBlank()) {
                throw new IllegalArgumentException("Payment method is required");
            }

            this.lastRequest = paymentRequest;
            return mockResponse;
        }

        @Override
        public PaymentResponse getPaymentByOrderId(Long orderId) {
            if (mockResponse != null && mockResponse.getOrderId().equals(orderId)) {
                return mockResponse;
            }
            throw new ResourceNotFoundException("Payment", "orderId", orderId);
        }

        public void setMockResponse(PaymentResponse response) {
            this.mockResponse = response;
        }

        public PaymentRequest getLastRequest() {
            return lastRequest;
        }
    }
}