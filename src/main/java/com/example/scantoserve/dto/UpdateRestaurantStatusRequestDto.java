package com.example.scantoserve.dto;

import com.example.scantoserve.entity.RestaurantStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateRestaurantStatusRequestDto(
        @NotNull(message = "Restaurant status is required")
        RestaurantStatus status
) {
}
