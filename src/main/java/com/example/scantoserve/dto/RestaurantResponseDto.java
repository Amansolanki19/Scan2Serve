package com.example.scantoserve.dto;

import com.example.scantoserve.entity.RestaurantStatus;

import java.time.LocalDateTime;

public record RestaurantResponseDto(


        Long id,
        String name,
        String description,
        String email,
        String phoneNumber,
        String addressLine,
        String city,
        String state,
        String postalCode,
        String country,
        RestaurantStatus status,
        Long ownerId,
        String ownerName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt

) {
}
