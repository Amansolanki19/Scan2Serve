package com.example.scantoserve.dto;

import java.util.List;

public record PublicMenuCategoryResponseDto(

        Long id,
        String name,
        String description,
        Integer displayOrder,
        List<PublicMenuItemResponseDto> items

) {
}