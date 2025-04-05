package com.foodordering.service;

import com.foodordering.dto.request.MenuItemRequest;
import com.foodordering.dto.response.MenuItemResponse;
import java.util.List;

public interface MenuService {
    List<MenuItemResponse> getAllMenuItems();
    List<MenuItemResponse> getAvailableMenuItems();
    MenuItemResponse getMenuItemById(Long id);
    MenuItemResponse createMenuItem(MenuItemRequest menuItemRequest);
    MenuItemResponse updateMenuItem(Long id, MenuItemRequest menuItemRequest);
    void deleteMenuItem(Long id);
}