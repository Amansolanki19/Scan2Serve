package com.example.scantoserve.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DiningTableRequestDto(
        @NotNull(message = "Table number is required")
        @Positive(message = "Table number must be positive")
        Integer tableNumber,

        @NotNull(message = "Capacity is required")
        @Min(value = 1, message = "Capacity must be at least 1")
        @Max(value = 50, message = "Capacity cannot exceed 50")
        Integer capacity
) {
}
