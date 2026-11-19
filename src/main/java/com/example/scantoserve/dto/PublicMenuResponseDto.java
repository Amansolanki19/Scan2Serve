package com.example.scantoserve.dto;

import java.util.List;

public record PublicMenuResponseDto(

        Long restaurantId,
        String restaurantName,
        String restaurantDescription,
        Long tableId,
        Integer tableNumber,
        Integer capacity,
        List<PublicMenuCategoryResponseDto> categories

) {
}