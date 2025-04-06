package com.foodordering.service;

import com.foodordering.dto.request.PaymentRequest;
import com.foodordering.dto.response.PaymentResponse;
import com.foodordering.exception.ResourceNotFoundException;
import com.foodordering.model.*;
import com.foodordering.repository.*;
import com.foodordering.service.impl.PaymentServiceImpl;
import com.foodordering.util.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest {

    private PaymentServiceImpl paymentService;
    private TestPaymentRepository paymentRepository;
    private TestOrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        paymentRepository = new TestPaymentRepository();
        orderRepository = new TestOrderRepository();
        paymentService = new PaymentServiceImpl(paymentRepository, orderRepository);
    }

    @Test
    void processPayment_ShouldProcessPaymentSuccessfully() {
        // Arrange
        Order order = createTestOrder(1L, BigDecimal.valueOf(50.00));
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1L);
        request.setPaymentMethod("CREDIT_CARD");

        // Act
        PaymentResponse response = paymentService.processPayment(request);

        // Assert
        assertNotNull(response);
        assertEquals("COMPLETED", response.getStatus());
        assertEquals(BigDecimal.valueOf(50.00), response.getAmount());
        assertEquals("CREDIT_CARD", response.getPaymentGateway());
        assertTrue(response.getTransactionId().startsWith("TXN"));
        
        // Verify order payment status was updated
        Order updatedOrder = orderRepository.findById(1L).orElseThrow();
        assertEquals("COMPLETED", updatedOrder.getPaymentStatus().name());
    }

    @Test
    void processPayment_ShouldThrowExceptionWhenOrderNotFound() {
        // Arrange
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(99L);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.processPayment(request);
        });
    }

    @Test
    void processPayment_ShouldThrowExceptionWhenPaymentExists() {
        // Arrange
        Order order = createTestOrder(1L, BigDecimal.valueOf(50.00));
        createTestPayment(1L, order);
        
        PaymentRequest request = new PaymentRequest();
        request.setOrderId(1L);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            paymentService.processPayment(request);
        });
    }

    @Test
    void getPaymentByOrderId_ShouldReturnPayment() {
        // Arrange
        Order order = createTestOrder(1L, BigDecimal.valueOf(50.00));
        createTestPayment(1L, order);

        // Act
        PaymentResponse response = paymentService.getPaymentByOrderId(1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getOrderId());
        assertEquals(BigDecimal.valueOf(50.00), response.getAmount());
    }

    @Test
    void getPaymentByOrderId_ShouldThrowExceptionWhenNotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.getPaymentByOrderId(99L);
        });
    }

    // Helper methods
    private Order createTestOrder(Long id, BigDecimal amount) {
        Order order = new Order();
        order.setId(id);
        order.setTotalAmount(amount);
        order.setPaymentStatus(AppConstants.PaymentStatus.PENDING);
        return orderRepository.save(order);
    }

    private Payment createTestPayment(Long id, Order order) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(AppConstants.PaymentStatus.COMPLETED);
        payment.setPaymentGateway("CREDIT_CARD");
        payment.setTransactionId("TXN12345");
        
        order.setPayment(payment);
        order.setPaymentStatus(AppConstants.PaymentStatus.COMPLETED);
        orderRepository.save(order);
        
        return paymentRepository.save(payment);
    }

    // Test repository implementations
    private static class TestPaymentRepository implements PaymentRepository {
        private final Map<Long, Payment> payments = new HashMap<>();
        private long nextId = 1;

        @Override
        public Payment save(Payment payment) {
            if (payment.getId() == null) {
                payment.setId(nextId++);
            }
            payments.put(payment.getId(), payment);
            return payment;
        }

        @Override
        public Optional<Payment> findByOrderId(Long orderId) {
            return payments.values().stream()
                    .filter(payment -> payment.getOrder().getId().equals(orderId))
                    .findFirst();
        }

        // Other required methods with empty implementations
        @Override public Optional<Payment> findById(Long id) { return Optional.empty(); }
        @Override public void delete(Payment payment) { }
        @Override public void deleteById(Long id) { }

		@Override
		public void flush() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public <S extends Payment> S saveAndFlush(S entity) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends Payment> List<S> saveAllAndFlush(Iterable<S> entities) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void deleteAllInBatch(Iterable<Payment> entities) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteAllByIdInBatch(Iterable<Long> ids) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteAllInBatch() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Payment getOne(Long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Payment getById(Long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Payment getReferenceById(Long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends Payment> List<S> findAll(Example<S> example) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends Payment> List<S> findAll(Example<S> example, Sort sort) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends Payment> List<S> saveAll(Iterable<S> entities) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Payment> findAll() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Payment> findAllById(Iterable<Long> ids) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean existsById(Long id) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public long count() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void deleteAllById(Iterable<? extends Long> ids) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteAll(Iterable<? extends Payment> entities) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteAll() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public List<Payment> findAll(Sort sort) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Page<Payment> findAll(Pageable pageable) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends Payment> Optional<S> findOne(Example<S> example) {
			// TODO Auto-generated method stub
			return Optional.empty();
		}

		@Override
		public <S extends Payment> Page<S> findAll(Example<S> example, Pageable pageable) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends Payment> long count(Example<S> example) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public <S extends Payment> boolean exists(Example<S> example) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public <S extends Payment, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
			// TODO Auto-generated method stub
			return null;
		}
    }

    private static class TestOrderRepository implements OrderRepository {
        private final Map<Long, Order> orders = new HashMap<>();
        private long nextId = 1;

        @Override
        public Order save(Order order) {
            if (order.getId() == null) {
                order.setId(nextId++);
            }
            orders.put(order.getId(), order);
            return order;
        }

        @Override
        public Optional<Order> findById(Long id) {
            return Optional.ofNullable(orders.get(id));
        }

        // Other required methods with empty implementations
        @Override public List<Order> findByUser(User user) { return null; }
        @Override public Optional<Order> findByIdAndUserId(Long orderId, Long userId) { return Optional.empty(); }
        @Override public List<Order> findAllByOrderByOrderDateDesc() { return null; }

		@Override
		public void flush() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public <S extends Order> S saveAndFlush(S entity) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends Order> List<S> saveAllAndFlush(Iterable<S> entities) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void deleteAllInBatch(Iterable<Order> entities) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteAllByIdInBatch(Iterable<Long> ids) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteAllInBatch() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Order getOne(Long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Order getById(Long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Order getReferenceById(Long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends Order> List<S> findAll(Example<S> example) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends Order> List<S> findAll(Example<S> example, Sort sort) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends Order> List<S> saveAll(Iterable<S> entities) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Order> findAll() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<Order> findAllById(Iterable<Long> ids) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean existsById(Long id) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public long count() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void deleteById(Long id) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void delete(Order entity) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteAllById(Iterable<? extends Long> ids) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteAll(Iterable<? extends Order> entities) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteAll() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public List<Order> findAll(Sort sort) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Page<Order> findAll(Pageable pageable) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends Order> Optional<S> findOne(Example<S> example) {
			// TODO Auto-generated method stub
			return Optional.empty();
		}

		@Override
		public <S extends Order> Page<S> findAll(Example<S> example, Pageable pageable) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends Order> long count(Example<S> example) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public <S extends Order> boolean exists(Example<S> example) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public <S extends Order, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
			// TODO Auto-generated method stub
			return null;
		}
    }
}