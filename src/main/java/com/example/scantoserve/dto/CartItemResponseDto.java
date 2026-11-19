package com.example.scantoserve.dto;

import java.math.BigDecimal;

public record CartItemResponseDto(

        Long cartItemId,

        Long menuItemId,

        String itemName,

        String description,

        String imageUrl,

        BigDecimal unitPrice,

        Integer quantity,

        BigDecimal subtotal

) {
}