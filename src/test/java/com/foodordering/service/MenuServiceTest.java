package com.foodordering.service;

import com.foodordering.dto.request.MenuItemRequest;
import com.foodordering.dto.response.MenuItemResponse;
import com.foodordering.exception.ResourceNotFoundException;
import com.foodordering.model.MenuItem;
import com.foodordering.model.User;
import com.foodordering.repository.MenuItemRepository;
import com.foodordering.repository.UserRepository;
import com.foodordering.service.impl.MenuServiceImpl;
import com.foodordering.util.AppConstants;
import com.foodordering.util.AppConstants.FoodCategory;
import com.mysql.cj.x.protobuf.MysqlxCrud.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class MenuServiceTest {

    private MenuServiceImpl menuService;
    private TestMenuItemRepository menuItemRepository;
    private TestUserRepository userRepository;
    
    private MenuItemRequest menuItemRequest;
    private MenuItem menuItem;
    private User adminUser;

    @BeforeEach
    void setUp() {
        menuItemRepository = new TestMenuItemRepository();
        userRepository = new TestUserRepository();
        menuService = new MenuServiceImpl(menuItemRepository, userRepository);
        
        // Setup test data
        menuItemRequest = new MenuItemRequest();
        menuItemRequest.setName("Pizza");
        menuItemRequest.setDescription("Delicious pizza");
        menuItemRequest.setPrice(BigDecimal.valueOf(10.99));
        menuItemRequest.setCategory(AppConstants.FoodCategory.MAIN_COURSE);
        menuItemRequest.setAvailable(true);
        menuItemRequest.setImageUrl("/images/pizza.jpg");
        menuItemRequest.setCreatedBy("admin");

        menuItem = new MenuItem();
        menuItem.setId(1L);
        menuItem.setName("Pizza");
        menuItem.setDescription("Delicious pizza");
        menuItem.setPrice(BigDecimal.valueOf(10.99));
        menuItem.setCategory(AppConstants.FoodCategory.MAIN_COURSE);
        menuItem.setAvailable(true);
        menuItem.setImageUrl("/images/pizza.jpg");

        adminUser = new User();
        adminUser.setUsername("admin");
        adminUser.setId(1L);
        userRepository.save(adminUser);

        // Setup security context
        SecurityContext securityContext = new TestSecurityContext();
        Authentication authentication = new TestAuthentication("admin");
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getAllMenuItems_ShouldReturnAllItems() {
        // Arrange
        menuItemRepository.save(menuItem);
        
        MenuItem secondItem = new MenuItem();
        secondItem.setId(2L);
        secondItem.setName("Burger");
        secondItem.setCategory(AppConstants.FoodCategory.MAIN_COURSE);
        menuItemRepository.save(secondItem);

        // Act
        List<MenuItemResponse> result = menuService.getAllMenuItems();

        // Assert
        assertEquals(2, result.size());
        assertEquals("Pizza", result.get(0).getName());
        assertEquals("Burger", result.get(1).getName());
    }

    @Test
    void getAvailableMenuItems_ShouldReturnOnlyAvailableItems() {
        // Arrange
        menuItemRepository.save(menuItem);
        
        MenuItem unavailableItem = new MenuItem();
        unavailableItem.setId(2L);
        unavailableItem.setName("Unavailable Burger");
        unavailableItem.setAvailable(false);
        unavailableItem.setCategory(AppConstants.FoodCategory.MAIN_COURSE);
        menuItemRepository.save(unavailableItem);

        // Act
        List<MenuItemResponse> result = menuService.getAvailableMenuItems();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Pizza", result.get(0).getName());
    }

    @Test
    void getMenuItemById_ShouldReturnItemWhenExists() {
        // Arrange
        menuItemRepository.save(menuItem);

        // Act
        MenuItemResponse result = menuService.getMenuItemById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("Pizza", result.getName());
        assertEquals(BigDecimal.valueOf(10.99), result.getPrice());
    }

    @Test
    void getMenuItemById_ShouldThrowExceptionWhenNotFound() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
            () -> menuService.getMenuItemById(99L));
        
        assertEquals("MenuItem not found with id : '99'", exception.getMessage());
    }

    @Test
    void createMenuItem_ShouldCreateNewItem() {
        // Act
        MenuItemResponse result = menuService.createMenuItem(menuItemRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Pizza", result.getName());
        assertEquals(1, menuItemRepository.count());
    }

   

    @Test
    void updateMenuItem_ShouldUpdateExistingItem() {
        // Arrange
        menuItemRepository.save(menuItem);
        
        MenuItemRequest updateRequest = new MenuItemRequest();
        updateRequest.setName("Updated Pizza");
        updateRequest.setPrice(BigDecimal.valueOf(12.99));
        updateRequest.setCategory(AppConstants.FoodCategory.MAIN_COURSE);

        // Act
        MenuItemResponse result = menuService.updateMenuItem(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated Pizza", result.getName());
        assertEquals(BigDecimal.valueOf(12.99), result.getPrice());
    }

    @Test
    void updateMenuItem_ShouldThrowExceptionWhenItemNotFound() {
        // Arrange
        MenuItemRequest updateRequest = new MenuItemRequest();
        updateRequest.setName("Updated Pizza");

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
            () -> menuService.updateMenuItem(99L, updateRequest));
        
        assertEquals("MenuItem not found with id : '99'", exception.getMessage());
    }

    @Test
    void deleteMenuItem_ShouldDeleteExistingItem() {
        // Arrange
        menuItemRepository.save(menuItem);

        // Act
        menuService.deleteMenuItem(1L);

        // Assert
        assertEquals(0, menuItemRepository.count());
    }

    @Test
    void deleteMenuItem_ShouldThrowExceptionWhenItemNotFound() {
        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
            () -> menuService.deleteMenuItem(99L));
        
        assertEquals("MenuItem not found with id : '99'", exception.getMessage());
    }

    // Test implementations of repositories and security classes
    private static class TestMenuItemRepository implements MenuItemRepository {
        private final List<MenuItem> items = new ArrayList<>();
        private long nextId = 1;

        @Override
        public List<MenuItem> findAll() {
            return new ArrayList<>(items);
        }

        @Override
        public List<MenuItem> findByAvailableTrue() {
            List<MenuItem> availableItems = new ArrayList<>();
            for (MenuItem item : items) {
                if (item.isAvailable()) {
                    availableItems.add(item);
                }
            }
            return availableItems;
        }

        @Override
        public Optional<MenuItem> findById(Long id) {
            return items.stream()
                .filter(item -> item.getId().equals(id))
                .findFirst();
        }

        @Override
        public MenuItem save(MenuItem menuItem) {
            if (menuItem.getId() == null) {
                menuItem.setId(nextId++);
            } else {
                deleteById(menuItem.getId());
            }
            items.add(menuItem);
            return menuItem;
        }

        @Override
        public void delete(MenuItem menuItem) {
            items.remove(menuItem);
        }

        @Override
        public void deleteById(Long id) {
            items.removeIf(item -> item.getId().equals(id));
        }

        public long count() {
            return items.size();
        }

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

    private static class TestUserRepository implements UserRepository {
        private final List<User> users = new ArrayList<>();
        private long nextId = 1;

        @Override
        public Optional<User> findByUsername(String username) {
            return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
        }

        @Override
        public User save(User user) {
            if (user.getId() == null) {
                user.setId(nextId++);
            }
            users.add(user);
            return user;
        }

        @Override
        public void deleteAll() {
            users.clear();
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
		public Optional<User> findById(Long id) {
			// TODO Auto-generated method stub
			return Optional.empty();
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

    private static class TestSecurityContext implements SecurityContext {
        private Authentication authentication;

        @Override
        public Authentication getAuthentication() {
            return authentication;
        }

        @Override
        public void setAuthentication(Authentication authentication) {
            this.authentication = authentication;
        }
    }

    private static class TestAuthentication implements Authentication {
        private final String username;

        public TestAuthentication(String username) {
            this.username = username;
        }

        @Override
        public String getName() {
            return username;
        }

        // Other required methods with default implementations
        @Override public Object getPrincipal() { return null; }
        @Override public Object getCredentials() { return null; }
        @Override public Object getDetails() { return null; }
        @Override public java.util.Collection getAuthorities() { return null; }
        @Override public boolean isAuthenticated() { return false; }
        @Override public void setAuthenticated(boolean isAuthenticated) { }
    }
}