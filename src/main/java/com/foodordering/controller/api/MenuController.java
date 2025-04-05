package com.foodordering.controller.api;

import com.foodordering.dto.request.MenuItemRequest;
import com.foodordering.dto.response.ApiResponse;
import com.foodordering.dto.response.MenuItemResponse;
import com.foodordering.service.MenuService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private static final Logger logger = LoggerFactory.getLogger(MenuController.class);
    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public ResponseEntity<List<MenuItemResponse>> getAllMenuItems() {
        logger.info("Fetching all menu items");
        return ResponseEntity.ok(menuService.getAvailableMenuItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MenuItemResponse> getMenuItemById(@PathVariable Long id) {
        logger.info("Fetching menu item by ID: {}", id);
        return ResponseEntity.ok(menuService.getMenuItemById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_STAFF')")
    public ResponseEntity<MenuItemResponse> createMenuItem(@Valid @RequestBody MenuItemRequest menuItemRequest) {
        logger.info("Creating new menu item: {}", menuItemRequest.getName());
        return ResponseEntity.ok(menuService.createMenuItem(menuItemRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_STAFF')")
    public ResponseEntity<MenuItemResponse> updateMenuItem(@PathVariable Long id, @Valid @RequestBody MenuItemRequest menuItemRequest) {
        logger.info("Updating menu item ID: {}", id);
        return ResponseEntity.ok(menuService.updateMenuItem(id, menuItemRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT_STAFF')")
    public ResponseEntity<ApiResponse> deleteMenuItem(@PathVariable Long id) {
        logger.info("Deleting menu item ID: {}", id);
        menuService.deleteMenuItem(id);
        return ResponseEntity.ok(new ApiResponse(true, "Menu item deleted successfully"));
    }
}
