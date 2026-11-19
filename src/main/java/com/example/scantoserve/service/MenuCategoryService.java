package com.example.scantoserve.service;

import com.example.scantoserve.dto.MenuCategoryRequestDto;
import com.example.scantoserve.dto.MenuCategoryResponseDto;

import java.util.List;

public interface MenuCategoryService {
    MenuCategoryResponseDto createCategory(
            Long restaurantId,
            MenuCategoryRequestDto request,
            String userEmail
    );

    List<MenuCategoryResponseDto> getRestaurantCategories(
            Long restaurantId,
            String userEmail
    );

    MenuCategoryResponseDto getCategoryById(
            Long restaurantId,
            Long categoryId,
            String userEmail
    );
}
