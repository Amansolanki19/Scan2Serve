package com.example.scantoserve.controller;

import com.example.scantoserve.common.ApiResponse;
import com.example.scantoserve.configuration.CustomUserDetails;
import com.example.scantoserve.dto.MenuItemRequestDto;
import com.example.scantoserve.dto.MenuItemResponseDto;
import com.example.scantoserve.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(
        "/api/restaurants/{restaurantId}/categories/{categoryId}/items"
)
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    // Create a menu item
    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> createMenuItem(
            @PathVariable Long restaurantId,
            @PathVariable Long categoryId,
            @Valid @RequestBody MenuItemRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        MenuItemResponseDto response = menuItemService.createMenuItem(
                restaurantId,
                categoryId,
                request,
                userDetails.getUsername()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Menu item created successfully",
                        response
                ));
    }

    // Get all items belonging to a category
    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuItemResponseDto>>> getCategoryItems(
            @PathVariable Long restaurantId,
            @PathVariable Long categoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<MenuItemResponseDto> response =
                menuItemService.getCategoryItems(
                        restaurantId,
                        categoryId,
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Menu items fetched successfully",
                        response
                )
        );
    }

    // Get a specific menu item
    @GetMapping("/{itemId}")
    public ResponseEntity<ApiResponse<MenuItemResponseDto>> getMenuItemById(
            @PathVariable Long restaurantId,
            @PathVariable Long categoryId,
            @PathVariable Long itemId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        MenuItemResponseDto response = menuItemService.getMenuItemById(
                restaurantId,
                categoryId,
                itemId,
                userDetails.getUsername()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Menu item fetched successfully",
                        response
                )
        );
    }
}