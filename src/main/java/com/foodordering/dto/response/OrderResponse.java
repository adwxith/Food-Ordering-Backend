package com.foodordering.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {
    private Long id;
    private Long userId;
    private String username;
    private LocalDateTime orderDate;
    private String deliveryAddress;
    private String status;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private List<OrderItemResponse> items;
    private String restaurantNotes;

    // Constructors
    public OrderResponse() {}

    public OrderResponse(Long id, Long userId, String username, LocalDateTime orderDate, 
                        String deliveryAddress, String status, BigDecimal totalAmount,
                        String paymentStatus, List<OrderItemResponse> items, String restaurantNotes) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.orderDate = orderDate;
        this.deliveryAddress = deliveryAddress;
        this.status = status;
        this.totalAmount = totalAmount;
        this.paymentStatus = paymentStatus;
        this.items = items;
        this.restaurantNotes = restaurantNotes;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public LocalDateTime getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(LocalDateTime orderDate) {
		this.orderDate = orderDate;
	}

	public String getDeliveryAddress() {
		return deliveryAddress;
	}

	public void setDeliveryAddress(String deliveryAddress) {
		this.deliveryAddress = deliveryAddress;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(String paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public List<OrderItemResponse> getItems() {
		return items;
	}

	public void setItems(List<OrderItemResponse> items) {
		this.items = items;
	}

	public String getRestaurantNotes() {
		return restaurantNotes;
	}

	public void setRestaurantNotes(String restaurantNotes) {
		this.restaurantNotes = restaurantNotes;
	}

    
}