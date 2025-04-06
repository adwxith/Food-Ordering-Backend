package com.foodordering.service;

import com.foodordering.dto.request.UpdateUserRequest;
import com.foodordering.dto.response.UserProfile;
import com.foodordering.exception.ResourceNotFoundException;
import com.foodordering.model.User;
import com.foodordering.repository.UserRepository;
import com.foodordering.service.impl.UserServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserServiceImpl userService;
    private TestUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new TestUserRepository();
        userService = new UserServiceImpl(userRepository);
    }

    @Test
    void getUserProfile_ShouldReturnUserProfile() {
        // Arrange
        User testUser = createTestUser(1L, "testuser", "Test User", "test@example.com");

        // Act
        UserProfile profile = userService.getUserProfile(1L);

        // Assert
        assertNotNull(profile);
        assertEquals("testuser", profile.getUsername());
        assertEquals("Test User", profile.getName());
        assertEquals("test@example.com", profile.getEmail());
    }

    @Test
    void getUserProfile_ShouldThrowExceptionWhenUserNotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserProfile(99L);
        });
    }

    @Test
    void updateUser_ShouldUpdateUserProfile() {
        // Arrange
        createTestUser(1L, "testuser", "Original Name", "original@example.com");
        
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("updated@example.com");
        updateRequest.setPhone("1234567890");
        updateRequest.setAddress("123 Main St");

        // Act
        UserProfile updatedProfile = userService.updateUser(1L, updateRequest);

        // Assert
        assertNotNull(updatedProfile);
        assertEquals("Updated Name", updatedProfile.getName());
        assertEquals("updated@example.com", updatedProfile.getEmail());
        assertEquals("1234567890", updatedProfile.getPhone());
        assertEquals("123 Main St", updatedProfile.getAddress());
        
        // Verify the username wasn't changed
        assertEquals("testuser", updatedProfile.getUsername());
    }

    @Test
    void updateUser_ShouldUpdatePartialFields() {
        // Arrange
        createTestUser(1L, "testuser", "Original Name", "original@example.com");
        
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setPhone("9876543210");

        // Act
        UserProfile updatedProfile = userService.updateUser(1L, updateRequest);

        // Assert
        assertNotNull(updatedProfile);
        assertEquals("Original Name", updatedProfile.getName()); // Should remain unchanged
        assertEquals("original@example.com", updatedProfile.getEmail()); // Should remain unchanged
        assertEquals("9876543210", updatedProfile.getPhone()); // Updated
        assertEquals("testuser", updatedProfile.getUsername()); // Should remain unchanged
    }

    @Test
    void updateUser_ShouldThrowExceptionWhenUserNotFound() {
        // Arrange
        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Name");

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(99L, updateRequest);
        });
    }

    @Test
    void deleteUser_ShouldDeleteUser() {
        // Arrange
        createTestUser(1L, "testuser", "Test User", "test@example.com");

        // Act
        userService.deleteUser(1L);

        // Assert
        assertFalse(userRepository.findById(1L).isPresent());
    }

    @Test
    void deleteUser_ShouldThrowExceptionWhenUserNotFound() {
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(99L);
        });
    }

    // Helper method to create test users
    private User createTestUser(Long id, String username, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setName(name);
        user.setEmail(email);
        return userRepository.save(user);
    }

    // Test implementation of UserRepository
    private static class TestUserRepository implements UserRepository {
        private final Map<Long, User> users = new HashMap<>();
        private long nextId = 1;

        @Override
        public Optional<User> findById(Long id) {
            return Optional.ofNullable(users.get(id));
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
        public void delete(User user) {
            users.remove(user.getId());
        }

        @Override
        public Optional<User> findByUsername(String username) {
            return users.values().stream()
                    .filter(user -> user.getUsername().equals(username))
                    .findFirst();
        }

        // Other required methods with empty implementations
        @Override public Optional<User> findByEmail(String email) { return Optional.empty(); }
        @Override public Boolean existsByUsername(String username) { return false; }
        @Override public Boolean existsByEmail(String email) { return false; }

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
		public Optional<User> findByUsernameOrEmail(String username, String email) {
			// TODO Auto-generated method stub
			return Optional.empty();
		}
    }
}