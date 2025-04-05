package com.foodordering.service.impl;

import com.foodordering.dto.request.MenuItemRequest;
import com.foodordering.dto.response.MenuItemResponse;
import com.foodordering.exception.ResourceNotFoundException;
import com.foodordering.model.MenuItem;
import com.foodordering.model.User;
import com.foodordering.repository.MenuItemRepository;
import com.foodordering.repository.UserRepository;
import com.foodordering.service.MenuService;
import com.foodordering.util.AppConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuServiceImpl implements MenuService {

    private static final Logger logger = LoggerFactory.getLogger(MenuServiceImpl.class);

    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    public MenuServiceImpl(MenuItemRepository menuItemRepository, 
                         UserRepository userRepository) {
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<MenuItemResponse> getAllMenuItems() {
        logger.info("Entering getAllMenuItems()");
        List<MenuItemResponse> items = menuItemRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        logger.info("Exiting getAllMenuItems() with {} items", items.size());
        return items;
    }

    @Override
    public List<MenuItemResponse> getAvailableMenuItems() {
        logger.info("Entering getAvailableMenuItems()");
        List<MenuItemResponse> items = menuItemRepository.findByAvailableTrue().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        logger.info("Exiting getAvailableMenuItems() with {} available items", items.size());
        return items;
    }

    @Override
    public MenuItemResponse getMenuItemById(Long id) {
        logger.info("Entering getMenuItemById() with id = {}", id);
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        MenuItemResponse response = convertToDto(menuItem);
        logger.info("Exiting getMenuItemById() with item = {}", response);
        return response;
    }

    @Override
    public MenuItemResponse createMenuItem(MenuItemRequest menuItemRequest) {
        logger.info("Entering createMenuItem() with request = {}", menuItemRequest);

        String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User createdBy = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        MenuItem menuItem = new MenuItem();
        menuItem.setName(menuItemRequest.getName());
        menuItem.setDescription(menuItemRequest.getDescription());
        menuItem.setPrice(menuItemRequest.getPrice());
        menuItem.setCategory(menuItemRequest.getCategory());
        menuItem.setAvailable(menuItemRequest.isAvailable());
        menuItem.setImageUrl(menuItemRequest.getImageUrl());
        menuItem.setCreatedBy(createdBy);

        MenuItem savedItem = menuItemRepository.save(menuItem);
        MenuItemResponse response = convertToDto(savedItem);
        logger.info("Exiting createMenuItem() with response = {}", response);
        return response;
    }

    @Override
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest menuItemRequest) {
        logger.info("Entering updateMenuItem() with id = {}, request = {}", id, menuItemRequest);
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));

        menuItem.setName(menuItemRequest.getName());
        menuItem.setDescription(menuItemRequest.getDescription());
        menuItem.setPrice(menuItemRequest.getPrice());
        menuItem.setCategory(menuItemRequest.getCategory());
        menuItem.setAvailable(menuItemRequest.isAvailable());
        menuItem.setImageUrl(menuItemRequest.getImageUrl());

        MenuItem updatedItem = menuItemRepository.save(menuItem);
        MenuItemResponse response = convertToDto(updatedItem);
        logger.info("Exiting updateMenuItem() with updated response = {}", response);
        return response;
    }

    @Override
    public void deleteMenuItem(Long id) {
        logger.info("Entering deleteMenuItem() with id = {}", id);
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MenuItem", "id", id));
        menuItemRepository.delete(menuItem);
        logger.info("Deleted menuItem with id = {}", id);
    }

    private MenuItemResponse convertToDto(MenuItem menuItem) {
        return new MenuItemResponse(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getCategory().name(),
                menuItem.isAvailable(),
                menuItem.getImageUrl()
        );
    }
}
