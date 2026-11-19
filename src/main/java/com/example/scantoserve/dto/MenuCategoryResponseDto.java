package com.example.scantoserve.dto;

import com.example.scantoserve.entity.MenuCategoryStatus;

import java.time.LocalDateTime;

public record MenuCategoryResponseDto(
        Long id,
        String name,
        String description,
        MenuCategoryStatus status,
        Integer displayOrder,
        Long restaurantId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
