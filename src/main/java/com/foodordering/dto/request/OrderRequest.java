package com.foodordering.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class OrderRequest {
    @NotBlank
    private String deliveryAddress;
    
    @NotEmpty
    private List<Item> items;
    
    private String restaurantNotes;

    public static class Item {
        @NotNull
        private Long menuItemId;
        
        @NotNull
        private Integer quantity;

        // Getters and Setters
        public Long getMenuItemId() {
            return menuItemId;
        }

        public void setMenuItemId(Long menuItemId) {
            this.menuItemId = menuItemId;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }
    }

    // Getters and Setters for OrderRequest
    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public String getRestaurantNotes() {
        return restaurantNotes;
    }

    public void setRestaurantNotes(String restaurantNotes) {
        this.restaurantNotes = restaurantNotes;
    }
}