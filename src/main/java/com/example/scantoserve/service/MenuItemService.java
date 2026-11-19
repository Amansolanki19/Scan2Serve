package com.example.scantoserve.service;

import com.example.scantoserve.dto.MenuItemRequestDto;
import com.example.scantoserve.dto.MenuItemResponseDto;

import java.util.List;

public interface MenuItemService {

    MenuItemResponseDto createMenuItem(
            Long restaurantId,
            Long categoryId,
            MenuItemRequestDto request,
            String userEmail
    );

    List<MenuItemResponseDto> getCategoryItems(
            Long restaurantId,
            Long categoryId,
            String userEmail
    );

    MenuItemResponseDto getMenuItemById(
            Long restaurantId,
            Long categoryId,
            Long itemId,
            String userEmail
    );
}
