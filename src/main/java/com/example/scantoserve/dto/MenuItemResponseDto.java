package com.example.scantoserve.dto;

import com.example.scantoserve.entity.FoodType;
import com.example.scantoserve.entity.MenuItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MenuItemResponseDto(
        Long id,
        String name,
        String description,
        BigDecimal price,
        BigDecimal discountPrice,
        FoodType foodType,
        String imageUrl,
        Integer preparationTime,
        MenuItemStatus status,
        Long categoryId,
        String categoryName,
        Long restaurantId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
