package com.foodordering.repository;

import com.foodordering.model.MenuItem;
import com.foodordering.util.AppConstants;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByAvailableTrue();
    List<MenuItem> findByCategoryAndAvailableTrue(AppConstants.FoodCategory category);
}