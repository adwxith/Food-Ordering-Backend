package com.foodordering.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import com.foodordering.util.AppConstants;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotNull
    private LocalDateTime orderDate;

    @NotBlank
    @Size(max = 500)
    private String deliveryAddress;

    @Enumerated(EnumType.STRING)
    private AppConstants.OrderStatus status;

    @NotNull
    private BigDecimal totalAmount = BigDecimal.ZERO;;

    @Enumerated(EnumType.STRING)
    private AppConstants.PaymentStatus paymentStatus;

    @Size(max = 50)
    private String paymentMethod;

    @Size(max = 500)
    private String restaurantNotes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OrderItem> items = new HashSet<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private Payment payment;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserFeedback feedback;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public AppConstants.OrderStatus getStatus() {
		return status;
	}

	public void setStatus(AppConstants.OrderStatus status) {
		this.status = status;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public AppConstants.PaymentStatus getPaymentStatus() {
		return paymentStatus;
	}

	public void setPaymentStatus(AppConstants.PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus;
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
	}

	public String getRestaurantNotes() {
		return restaurantNotes;
	}

	public void setRestaurantNotes(String restaurantNotes) {
		this.restaurantNotes = restaurantNotes;
	}

	public Set<OrderItem> getItems() {
		return items;
	}

	public void setItems(Set<OrderItem> items) {
		this.items = items;
	}

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}

	public UserFeedback getFeedback() {
		return feedback;
	}

	public void setFeedback(UserFeedback feedback) {
		this.feedback = feedback;
	}
	@PrePersist
    @PreUpdate
    private void calculateTotalAmount() {
        if (this.totalAmount == null) {
            this.totalAmount = BigDecimal.ZERO;
        }
        if (this.items != null) {
            this.totalAmount = this.items.stream()
                .map(item -> item.getPriceAtOrderTime().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    
}