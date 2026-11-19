package com.example.scantoserve.dto;

import com.example.scantoserve.entity.FoodType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record MenuItemRequestDto(
        @NotBlank(message = "Item name is required")
        @Size(max = 150, message = "Item name cannot exceed 150 characters")
        String name,

        @Size(max = 1000, message = "Description cannot exceed 1000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Invalid price format")
        BigDecimal price,

        @DecimalMin(value = "0.01", message = "Discount price must be greater than zero")
        @Digits(integer = 8, fraction = 2, message = "Invalid discount price format")
        BigDecimal discountPrice,

        @NotNull(message = "Food type is required")
        FoodType foodType,

        @Size(max = 1000, message = "Image URL is too long")
        String imageUrl,

        @Min(value = 1, message = "Preparation time must be at least 1 minute")
        @Max(value = 300, message = "Preparation time cannot exceed 300 minutes")
        Integer preparationTime

) {
}
