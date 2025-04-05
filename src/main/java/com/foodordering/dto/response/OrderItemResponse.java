package com.foodordering.dto.response;

import java.math.BigDecimal;

public class OrderItemResponse {
    private Long id;
    private Long menuItemId;
    private String menuItemName;
    private Integer quantity;
    private BigDecimal priceAtOrderTime;
    private BigDecimal subtotal;

    public OrderItemResponse(Long id, Long menuItemId, String menuItemName, 
                           Integer quantity, BigDecimal priceAtOrderTime, BigDecimal subtotal) {
        this.id = id;
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.quantity = quantity;
        this.priceAtOrderTime = priceAtOrderTime;
        this.subtotal = subtotal;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public Long getMenuItemId() {
        return menuItemId;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPriceAtOrderTime() {
        return priceAtOrderTime;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}