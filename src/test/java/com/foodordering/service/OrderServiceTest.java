package com.foodordering.service;

import com.foodordering.dto.request.OrderRequest;
import com.foodordering.dto.response.OrderItemResponse;
import com.foodordering.dto.response.OrderResponse;
import com.foodordering.exception.*;
import com.foodordering.model.*;
import com.foodordering.repository.*;
import com.foodordering.service.impl.OrderServiceImpl;
import com.foodordering.util.AppConstants;
import com.foodordering.util.AppConstants.FoodCategory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderServiceImpl orderService;
    private TestOrderRepository orderRepository;
    private TestUserRepository userRepository;
    private TestMenuItemRepository menuItemRepository;

    @BeforeEach
    void setUp() {
        orderRepository = new TestOrderRepository();
        userRepository = new TestUserRepository();
        menuItemRepository = new TestMenuItemRepository();
        orderService = new OrderServiceImpl(orderRepository, userRepository, menuItemRepository);
    }

    @Test
    void createOrder_ShouldCreateNewOrder() {
        // Arrange
        User user = createTestUser(1L, "customer");
        MenuItem menuItem = createTestMenuItem(1L, "Pizza", BigDecimal.valueOf(10.99));
        
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setDeliveryAddress("123 Main St");
        OrderRequest.Item item = new OrderRequest.Item();
        item.setMenuItemId(1L);
        item.setQuantity(2);
        orderRequest.setItems(List.of(item));

        // Act
        OrderResponse response = orderService.createOrder(orderRequest, 1L);

        // Assert
        assertNotNull(response);
        assertEquals("PENDING", response.getStatus());
        assertEquals(1, orderRepository.count());
        assertEquals(BigDecimal.valueOf(21.98), response.getTotalAmount());
        assertEquals("PENDING", response.getPaymentStatus());
    }

    @Test
    void createOrder_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setDeliveryAddress("123 Main St");

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(orderRequest, 99L);
        });
    }

    @Test
    void createOrder_ShouldThrowExceptionWhenMenuItemNotFound() {
        // Arrange
        User user = createTestUser(1L, "customer");
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setDeliveryAddress("123 Main St");
        OrderRequest.Item item = new OrderRequest.Item();
        item.setMenuItemId(99L);
        orderRequest.setItems(List.of(item));

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.createOrder(orderRequest, 1L);
        });
    }

    @Test
    void getOrderById_ShouldReturnOrder() {
        // Arrange
        User user = createTestUser(1L, "customer");
        Order order = createTestOrder(1L, user);
        
        // Act
        OrderResponse response = orderService.getOrderById(1L, 1L);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("PENDING", response.getPaymentStatus());
    }

    @Test
    void getOrderById_ShouldThrowExceptionWhenOrderNotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.getOrderById(99L, 1L);
        });
    }

    @Test
    void getOrdersByUser_ShouldReturnUserOrders() {
        // Arrange
        User user = createTestUser(1L, "customer");
        Order order1 = createTestOrder(1L, user);
        Order order2 = createTestOrder(2L, user);
        
        // Act
        List<OrderResponse> responses = orderService.getOrdersByUser(1L);

        // Assert
        assertEquals(2, responses.size());
        assertEquals("PENDING", responses.get(0).getPaymentStatus());
    }

    @Test
    void updateOrderStatus_ShouldUpdateStatus() {
        // Arrange
        User user = createTestUser(1L, "customer");
        Order order = createTestOrder(1L, user);
        
        // Act
        OrderResponse response = orderService.updateOrderStatus(1L, "PREPARING");

        // Assert
        assertEquals("PREPARING", response.getStatus());
        assertEquals("PENDING", response.getPaymentStatus());
    }

    @Test
    void updateOrderStatus_ShouldThrowExceptionForInvalidStatus() {
        // Arrange
        User user = createTestUser(1L, "customer");
        Order order = createTestOrder(1L, user);
        
        // Act & Assert
        assertThrows(InvalidStatusException.class, () -> {
            orderService.updateOrderStatus(1L, "INVALID_STATUS");
        });
    }

    @Test
    void cancelOrder_ShouldCancelPendingOrder() {
        // Arrange
        User user = createTestUser(1L, "customer");
        Order order = createTestOrder(1L, user);
        
        // Act
        orderService.cancelOrder(1L, 1L);

        // Assert
        Order cancelledOrder = orderRepository.findById(1L).orElseThrow();
        assertEquals("CANCELLED", cancelledOrder.getStatus().name());
    }

    @Test
    void cancelOrder_ShouldThrowExceptionForNonPendingOrder() {
        // Arrange
        User user = createTestUser(1L, "customer");
        Order order = createTestOrder(1L, user);
        order.setStatus(AppConstants.OrderStatus.PREPARING);
        orderRepository.save(order);
        
        // Act & Assert
        assertThrows(OrderProcessingException.class, () -> {
            orderService.cancelOrder(1L, 1L);
        });
    }

    @Test
    void getAllOrders_ShouldReturnAllOrders() {
        // Arrange
        User user = createTestUser(1L, "customer");
        Order order1 = createTestOrder(1L, user);
        Order order2 = createTestOrder(2L, user);
        
        // Act
        List<OrderResponse> responses = orderService.getAllOrders();

        // Assert
        assertEquals(2, responses.size());
        assertEquals("PENDING", responses.get(0).getPaymentStatus());
    }

    // Helper methods
    private User createTestUser(Long id, String username) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        return userRepository.save(user);
    }

    private MenuItem createTestMenuItem(Long id, String name, BigDecimal price) {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(id);
        menuItem.setName(name);
        menuItem.setPrice(price);
        menuItem.setCategory(AppConstants.FoodCategory.MAIN_COURSE);
        return menuItemRepository.save(menuItem);
    }

    private Order createTestOrder(Long id, User user) {
        Order order = new Order();
        order.setId(id);
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(AppConstants.OrderStatus.PENDING);
        order.setPaymentStatus(AppConstants.PaymentStatus.PENDING);
        order.setTotalAmount(BigDecimal.ZERO);
        
        // Add at least one order item to ensure total amount calculation works
        MenuItem menuItem = createTestMenuItem(id, "Test Item", BigDecimal.TEN);
        OrderItem orderItem = new OrderItem();
        orderItem.setMenuItem(menuItem);
        orderItem.setQuantity(1);
        orderItem.setPriceAtOrderTime(menuItem.getPrice());
        order.getItems().add(orderItem);
        
        // Calculate total amount
        order.setTotalAmount(menuItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        
        return orderRepository.save(order);
    }

    // Test repository implementations
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

        @Override
        public List<Order> findByUser(User user) {
            return orders.values().stream()
                    .filter(order -> order.getUser().getId().equals(user.getId()))
                    .toList();
        }

        @Override
        public Optional<Order> findByIdAndUserId(Long orderId, Long userId) {
            return orders.values().stream()
                    .filter(order -> order.getId().equals(orderId) && 
                            order.getUser().getId().equals(userId))
                    .findFirst();
        }

        @Override
        public List<Order> findAllByOrderByOrderDateDesc() {
            return new ArrayList<>(orders.values());
        }

        public long count() {
            return orders.size();
        }

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

    private static class TestUserRepository implements UserRepository {
        private final Map<Long, User> users = new HashMap<>();
        private long nextId = 1;

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(users.get(id));
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return users.values().stream()
                    .filter(user -> user.getUsername().equals(username))
                    .findFirst();
        }

        @Override
        public User save(User user) {
            if (user.getId() == null) {
                user.setId(nextId++);
            }
            users.put(user.getId(), user);
            return user;
        }

		@Override
		public void flush() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public <S extends User> S saveAndFlush(S entity) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends User> List<S> saveAllAndFlush(Iterable<S> entities) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void deleteAllInBatch(Iterable<User> entities) {
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
		public User getOne(Long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public User getById(Long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public User getReferenceById(Long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends User> List<S> findAll(Example<S> example) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends User> List<S> findAll(Example<S> example, Sort sort) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends User> List<S> saveAll(Iterable<S> entities) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<User> findAll() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<User> findAllById(Iterable<Long> ids) {
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
		public void delete(User entity) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteAllById(Iterable<? extends Long> ids) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteAll(Iterable<? extends User> entities) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteAll() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public List<User> findAll(Sort sort) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Page<User> findAll(Pageable pageable) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends User> Optional<S> findOne(Example<S> example) {
			// TODO Auto-generated method stub
			return Optional.empty();
		}

		@Override
		public <S extends User> Page<S> findAll(Example<S> example, Pageable pageable) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends User> long count(Example<S> example) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public <S extends User> boolean exists(Example<S> example) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public <S extends User, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Optional<User> findByEmail(String email) {
			// TODO Auto-generated method stub
			return Optional.empty();
		}

		@Override
		public Optional<User> findByUsernameOrEmail(String username, String email) {
			// TODO Auto-generated method stub
			return Optional.empty();
		}

		@Override
		public Boolean existsByUsername(String username) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Boolean existsByEmail(String email) {
			// TODO Auto-generated method stub
			return null;
		}
    }

    private static class TestMenuItemRepository implements MenuItemRepository {
        private final Map<Long, MenuItem> menuItems = new HashMap<>();
        private long nextId = 1;

        @Override
        public Optional<MenuItem> findById(Long id) {
            return Optional.ofNullable(menuItems.get(id));
        }

        @Override
        public MenuItem save(MenuItem menuItem) {
            if (menuItem.getId() == null) {
                menuItem.setId(nextId++);
            }
            menuItems.put(menuItem.getId(), menuItem);
            return menuItem;
        }

        // Other required methods with empty implementations
        @Override public List<MenuItem> findAll() { return null; }
        @Override public List<MenuItem> findByAvailableTrue() { return null; }
        @Override public void delete(MenuItem menuItem) { }
        @Override public void deleteById(Long id) { }

		@Override
		public void flush() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public <S extends MenuItem> S saveAndFlush(S entity) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends MenuItem> List<S> saveAllAndFlush(Iterable<S> entities) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void deleteAllInBatch(Iterable<MenuItem> entities) {
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
		public MenuItem getOne(Long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public MenuItem getById(Long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public MenuItem getReferenceById(Long id) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends MenuItem> List<S> findAll(Example<S> example) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends MenuItem> List<S> findAll(Example<S> example, Sort sort) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends MenuItem> List<S> saveAll(Iterable<S> entities) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<MenuItem> findAllById(Iterable<Long> ids) {
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
		public void deleteAll(Iterable<? extends MenuItem> entities) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void deleteAll() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public List<MenuItem> findAll(Sort sort) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Page<MenuItem> findAll(Pageable pageable) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends MenuItem> Optional<S> findOne(Example<S> example) {
			// TODO Auto-generated method stub
			return Optional.empty();
		}

		@Override
		public <S extends MenuItem> Page<S> findAll(Example<S> example, Pageable pageable) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <S extends MenuItem> long count(Example<S> example) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public <S extends MenuItem> boolean exists(Example<S> example) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public <S extends MenuItem, R> R findBy(Example<S> example,
				Function<FetchableFluentQuery<S>, R> queryFunction) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public List<MenuItem> findByCategoryAndAvailableTrue(FoodCategory category) {
			// TODO Auto-generated method stub
			return null;
		}
    }
}