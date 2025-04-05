
package com.foodordering.service;

import com.foodordering.dto.request.PaymentRequest;
import com.foodordering.dto.response.PaymentResponse;

public interface PaymentService {
    PaymentResponse processPayment(PaymentRequest paymentRequest);
    PaymentResponse getPaymentByOrderId(Long orderId);
}