package com.example.scantoserve.controller;

import com.example.scantoserve.common.ApiResponse;
import com.example.scantoserve.configuration.CustomUserDetails;
import com.example.scantoserve.dto.MenuCategoryRequestDto;
import com.example.scantoserve.dto.MenuCategoryResponseDto;
import com.example.scantoserve.service.MenuCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/categories")
@RequiredArgsConstructor
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;

    // Create a menu category
    @PostMapping
    public ResponseEntity<ApiResponse<MenuCategoryResponseDto>> createCategory(
            @PathVariable Long restaurantId,
            @Valid @RequestBody MenuCategoryRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        MenuCategoryResponseDto response =
                menuCategoryService.createCategory(
                        restaurantId,
                        request,
                        userDetails.getUsername()
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Menu category created successfully",
                        response
                ));
    }

    // Get all categories of a restaurant
    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuCategoryResponseDto>>> getRestaurantCategories(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<MenuCategoryResponseDto> response =
                menuCategoryService.getRestaurantCategories(
                        restaurantId,
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Menu categories fetched successfully",
                        response
                )
        );
    }

    // Get a specific category
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResponse<MenuCategoryResponseDto>> getCategoryById(
            @PathVariable Long restaurantId,
            @PathVariable Long categoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        MenuCategoryResponseDto response =
                menuCategoryService.getCategoryById(
                        restaurantId,
                        categoryId,
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Menu category fetched successfully",
                        response
                )
        );
    }
}