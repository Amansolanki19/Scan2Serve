package com.example.scantoserve.dto;

import java.math.BigDecimal;

public record OrderItemResponseDto(

        Long id,

        Long menuItemId,

        String itemName,

        String description,

        BigDecimal unitPrice,

        Integer quantity,

        BigDecimal subtotal

) {
}