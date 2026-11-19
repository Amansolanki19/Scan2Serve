package com.example.scantoserve.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MenuCategoryRequestDto(


        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name cannot exceed 100 characters")
                String name,

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,

        @Min(value = 0, message = "Display order cannot be negative")
        @Max(value = 1000, message = "Display order cannot exceed 1000")
        Integer displayOrder
) {
}

