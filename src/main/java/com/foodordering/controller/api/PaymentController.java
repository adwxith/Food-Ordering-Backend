package com.foodordering.controller.api;

import com.foodordering.dto.request.PaymentRequest;
import com.foodordering.dto.response.ApiResponse;
import com.foodordering.dto.response.PaymentResponse;
import com.foodordering.security.CurrentUser;
import com.foodordering.security.UserPrincipal;
import com.foodordering.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponse> processPayment(@CurrentUser UserPrincipal currentUser, @Valid @RequestBody PaymentRequest paymentRequest) {
        logger.info("Processing payment for user ID: {}", currentUser.getId());
        return ResponseEntity.ok(paymentService.processPayment(paymentRequest));
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponse> getPaymentByOrderId(@CurrentUser UserPrincipal currentUser, @PathVariable Long orderId) {
        logger.info("Fetching payment details for order ID: {} and user ID: {}", orderId, currentUser.getId());
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }
}
