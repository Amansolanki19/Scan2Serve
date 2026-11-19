package com.example.scantoserve.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CartResponseDto(

        Long cartId,

        Long restaurantId,

        String restaurantName,

        Long diningTableId,

        Integer tableNumber,

        List<CartItemResponseDto> items,

        Integer totalItems,

        BigDecimal totalAmount,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}