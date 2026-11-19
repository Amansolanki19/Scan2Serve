package com.example.scantoserve.dto;

import com.example.scantoserve.entity.FoodType;

import java.math.BigDecimal;

public record PublicMenuItemResponseDto(

        Long id,
        String name,
        String description,
        BigDecimal price,
        BigDecimal discountPrice,
        FoodType foodType,
        String imageUrl,
        Integer preparationTime

) {
}