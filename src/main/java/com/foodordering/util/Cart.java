package com.foodordering.util;

import com.foodordering.dto.response.MenuItemResponse;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class Cart {
    private final Map<Long, CartItem> items = new HashMap<>();
    private BigDecimal total = BigDecimal.ZERO;

    public void addItem(MenuItemResponse menuItem) {
        CartItem cartItem = items.getOrDefault(menuItem.getId(), 
            new CartItem(menuItem, 0));
        cartItem.incrementQuantity();
        items.put(menuItem.getId(), cartItem);
        recalculateTotal();
    }

    public void removeItem(Long menuItemId) {
        items.remove(menuItemId);
        recalculateTotal();
    }

    public Map<Long, CartItem> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    private void recalculateTotal() {
        total = items.values().stream()
            .map(CartItem::getSubtotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static class CartItem {
        private final MenuItemResponse menuItem;
        private int quantity;

        public CartItem(MenuItemResponse menuItem, int quantity) {
            this.menuItem = menuItem;
            this.quantity = quantity;
        }

        public void incrementQuantity() {
            quantity++;
        }

        public MenuItemResponse getMenuItem() {
            return menuItem;
        }

        public int getQuantity() {
            return quantity;
        }

        public BigDecimal getSubtotal() {
            return menuItem.getPrice().multiply(BigDecimal.valueOf(quantity));
        }
    }
}