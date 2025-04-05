package com.foodordering.util;

public class AppConstants {
    public enum RoleName {
        ROLE_CUSTOMER,
        ROLE_RESTAURANT_STAFF,
        ROLE_ADMIN
    }

    // Add other constants as needed
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "10";
    public static final int MAX_PAGE_SIZE = 50;
    
    public enum OrderStatus {
        PENDING, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    }
    
    public enum PaymentStatus {
        PENDING, COMPLETED, FAILED, REFUNDED
    }
    public enum FoodCategory {
    	MAIN_COURSE,
        STARTERS,
        BEVERAGES,
        PIZZA  }
}