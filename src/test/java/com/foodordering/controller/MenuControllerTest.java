package com.foodordering.controller;

import com.foodordering.controller.api.MenuController;
import com.foodordering.dto.request.MenuItemRequest;
import com.foodordering.dto.response.ApiResponse;
import com.foodordering.dto.response.MenuItemResponse;
import com.foodordering.exception.ResourceNotFoundException;
import com.foodordering.service.MenuService;
import com.foodordering.util.AppConstants;
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
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MenuControllerTest {

    private MenuController menuController;
    private TestMenuService menuService;
    
    private MenuItemRequest menuItemRequest;
    private MenuItemResponse menuItemResponse;

    @BeforeEach
    void setUp() {
        menuService = new TestMenuService();
        menuController = new MenuController(menuService);
        
        menuItemRequest = new MenuItemRequest();
        menuItemRequest.setName("Soda");
        menuItemRequest.setDescription("Premium soda");
        menuItemRequest.setPrice(BigDecimal.valueOf(5.00));
        menuItemRequest.setCategory(AppConstants.FoodCategory.BEVERAGES);
        menuItemRequest.setAvailable(true);
        menuItemRequest.setImageUrl("/images/soda.jpg");
        menuItemRequest.setCreatedBy("admin");
        
        menuItemResponse = new MenuItemResponse();
        menuItemResponse.setId(1L);
        menuItemResponse.setName("Soda");
        menuItemResponse.setDescription("Premium soda");
        menuItemResponse.setPrice(BigDecimal.valueOf(5.00));
        menuItemResponse.setCategory("BEVERAGES");
        menuItemResponse.setAvailable(true);
        menuItemResponse.setImageUrl("/images/soda.jpg");
    }

    @Test
    void getAllMenuItems_ShouldReturnAvailableItems() {
        menuService.addMenuItem(menuItemResponse);
        
        MenuItemResponse pizza = new MenuItemResponse();
        pizza.setId(2L);
        pizza.setName("Pizza");
        pizza.setCategory("MAIN_COURSE");
        pizza.setAvailable(false);
        menuService.addMenuItem(pizza);
        
        ResponseEntity<List<MenuItemResponse>> response = menuController.getAllMenuItems();
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Soda", response.getBody().get(0).getName());
    }

    @Test
    void getMenuItemById_ShouldReturnItemWhenExists() {
        menuService.addMenuItem(menuItemResponse);
        
        ResponseEntity<MenuItemResponse> response = menuController.getMenuItemById(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Soda", response.getBody().getName());
    }

    @Test
    void getMenuItemById_ShouldThrowNotFoundWhenNotExists() {
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            menuController.getMenuItemById(99L);
        });
        
        assertEquals("Menu item not found with id : '99'", exception.getMessage());
    }

    @Test
    void createMenuItem_ShouldCreateItemWithAdminRole() {
        setupSecurityContext("admin", "ROLE_ADMIN");
        
        ResponseEntity<MenuItemResponse> response = menuController.createMenuItem(menuItemRequest);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Soda", response.getBody().getName());
        assertEquals(1, menuService.getMenuItemsCount());
    }

    @Test
    void createMenuItem_ShouldDenyWithoutProperRole() {
        setupSecurityContext("user", "ROLE_CUSTOMER");
        
        Exception exception = assertThrows(ResponseStatusException.class, () -> {
            menuController.createMenuItem(menuItemRequest);
        });
        
        assertEquals(HttpStatus.FORBIDDEN, ((ResponseStatusException) exception).getStatusCode());
        assertEquals("403 FORBIDDEN \"Access denied\"", exception.getMessage());
    }

    @Test
    void updateMenuItem_ShouldUpdateExistingItem() {
        setupSecurityContext("admin", "ROLE_ADMIN");
        menuService.addMenuItem(menuItemResponse);
        
        MenuItemRequest updateRequest = new MenuItemRequest();
        updateRequest.setName("Premium Soda");
        updateRequest.setPrice(BigDecimal.valueOf(6.00));
        updateRequest.setCategory(AppConstants.FoodCategory.BEVERAGES);
        
        ResponseEntity<MenuItemResponse> response = menuController.updateMenuItem(1L, updateRequest);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Premium Soda", response.getBody().getName());
        assertEquals(BigDecimal.valueOf(6.00), response.getBody().getPrice());
    }

    @Test
    void updateMenuItem_ShouldThrowNotFoundForNonExistingItem() {
        setupSecurityContext("admin", "ROLE_ADMIN");
        
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            menuController.updateMenuItem(99L, menuItemRequest);
        });
        
        assertEquals("Menu item not found with id : '99'", exception.getMessage());
    }

    @Test
    void deleteMenuItem_ShouldDeleteExistingItem() {
        setupSecurityContext("admin", "ROLE_ADMIN");
        menuService.addMenuItem(menuItemResponse);
        
        ResponseEntity<ApiResponse> response = menuController.deleteMenuItem(1L);
        
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getSuccess());
        assertEquals(0, menuService.getMenuItemsCount());
    }

    @Test
    void deleteMenuItem_ShouldThrowNotFoundForNonExistingItem() {
        setupSecurityContext("admin", "ROLE_ADMIN");
        
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            menuController.deleteMenuItem(99L);
        });
        
        assertEquals("Menu item not found with id : '99'", exception.getMessage());
    }

    private void setupSecurityContext(String username, String role) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(role));
        
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            username, "password", authorities);
        
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    private static class TestMenuService implements MenuService {
        private final List<MenuItemResponse> menuItems = new ArrayList<>();
        private long nextId = 1;

        @Override
        public List<MenuItemResponse> getAllMenuItems() {
            return new ArrayList<>(menuItems);
        }

        @Override
        public List<MenuItemResponse> getAvailableMenuItems() {
            List<MenuItemResponse> availableItems = new ArrayList<>();
            for (MenuItemResponse item : menuItems) {
                if (item.isAvailable()) {
                    availableItems.add(item);
                }
            }
            return availableItems;
        }

        @Override
        public MenuItemResponse getMenuItemById(Long id) {
            return menuItems.stream()
                .filter(i -> i.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Menu item", "id", id));
        }

        @Override
        public MenuItemResponse createMenuItem(MenuItemRequest menuItemRequest) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || 
                             a.getAuthority().equals("ROLE_RESTAURANT_STAFF"))) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
            }

            MenuItemResponse response = new MenuItemResponse();
            response.setId(nextId++);
            response.setName(menuItemRequest.getName());
            response.setPrice(menuItemRequest.getPrice());
            response.setCategory(menuItemRequest.getCategory().toString());
            menuItems.add(response);
            return response;
        }

        @Override
        public MenuItemResponse updateMenuItem(Long id, MenuItemRequest menuItemRequest) {
            MenuItemResponse existing = getMenuItemById(id);
            existing.setName(menuItemRequest.getName());
            existing.setPrice(menuItemRequest.getPrice());
            return existing;
        }

        @Override
        public void deleteMenuItem(Long id) {
            if (!menuItems.removeIf(item -> item.getId().equals(id))) {
                throw new ResourceNotFoundException("Menu item", "id", id);
            }
        }

        public void addMenuItem(MenuItemResponse item) {
            if (item.getId() == null) {
                item.setId(nextId++);
            }
            menuItems.add(item);
        }

        public int getMenuItemsCount() {
            return menuItems.size();
        }
    }
}