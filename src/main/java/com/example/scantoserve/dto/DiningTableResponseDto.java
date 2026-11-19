package com.example.scantoserve.dto;

import com.example.scantoserve.entity.DiningTableStatus;

import java.time.LocalDateTime;

public record DiningTableResponseDto(
        Long id,
        Integer tableNumber,
        Integer capacity,
        DiningTableStatus status,
        String qrToken,
        Long restaurantId,
        LocalDateTime createdAt
) {
}
