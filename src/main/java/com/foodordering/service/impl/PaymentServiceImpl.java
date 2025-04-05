package com.foodordering.service.impl;

import com.foodordering.dto.request.PaymentRequest;
import com.foodordering.dto.response.PaymentResponse;
import com.foodordering.exception.ResourceNotFoundException;
import com.foodordering.model.Order;
import com.foodordering.model.Payment;
import com.foodordering.repository.OrderRepository;
import com.foodordering.repository.PaymentRepository;
import com.foodordering.service.PaymentService;
import com.foodordering.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                              OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public PaymentResponse processPayment(PaymentRequest paymentRequest) {
        logger.info("Processing payment for Order ID: {}", paymentRequest.getOrderId());

        Order order = orderRepository.findById(paymentRequest.getOrderId())
                .orElseThrow(() -> {
                    logger.error("Order not found with ID: {}", paymentRequest.getOrderId());
                    return new ResourceNotFoundException("Order", "id", paymentRequest.getOrderId());
                });

        if (order.getPayment() != null) {
            logger.warn("Payment already exists for Order ID: {}", paymentRequest.getOrderId());
            throw new IllegalStateException("Payment already processed for this order");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentGateway(paymentRequest.getPaymentMethod());
        payment.setTransactionId(generateTransactionId());

        logger.debug("Simulating payment processing for Order ID: {}", paymentRequest.getOrderId());
        payment.setStatus(AppConstants.PaymentStatus.COMPLETED);

        Payment savedPayment = paymentRepository.save(payment);
        logger.info("Payment saved with Transaction ID: {}", savedPayment.getTransactionId());

        order.setPaymentStatus(AppConstants.PaymentStatus.COMPLETED);
        orderRepository.save(order);
        logger.info("Order payment status updated to COMPLETED for Order ID: {}", order.getId());

        return convertToDto(savedPayment);
    }

    @Override
    public PaymentResponse getPaymentByOrderId(Long orderId) {
        logger.info("Fetching payment details for Order ID: {}", orderId);

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    logger.error("Payment not found for Order ID: {}", orderId);
                    return new ResourceNotFoundException("Payment", "orderId", orderId);
                });

        logger.debug("Payment details found for Order ID: {}", orderId);
        return convertToDto(payment);
    }

    private String generateTransactionId() {
        String txnId = "TXN" + System.currentTimeMillis();
        logger.debug("Generated Transaction ID: {}", txnId);
        return txnId;
    }

    private PaymentResponse convertToDto(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getTransactionId(),
                payment.getPaymentGateway(),
                payment.getStatus().name()
        );
    }
}
