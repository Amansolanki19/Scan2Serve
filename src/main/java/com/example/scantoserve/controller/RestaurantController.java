package com.example.scantoserve.controller;

import com.example.scantoserve.common.ApiResponse;
import com.example.scantoserve.configuration.CustomUserDetails;
import com.example.scantoserve.dto.RestaurantRequestDto;
import com.example.scantoserve.dto.RestaurantResponseDto;
import com.example.scantoserve.dto.UpdateRestaurantStatusRequestDto;
import com.example.scantoserve.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurants")
public class RestaurantController {
    private final RestaurantService restaurantService;

    @PostMapping
    public ResponseEntity<ApiResponse<RestaurantResponseDto>>
    createRestaurant(
            @Valid @RequestBody RestaurantRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) throws AccessDeniedException {

        RestaurantResponseDto response =
                restaurantService.createRestaurant(
                        request,
                        userDetails.getUsername()
                );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Restaurant created successfully",
                        response
                ));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<RestaurantResponseDto>>>
    getMyRestaurants(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        List<RestaurantResponseDto> restaurants =
                restaurantService.getMyRestaurants(
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurants retrieved successfully",
                        restaurants
                )
        );
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<RestaurantResponseDto>>
    getRestaurantById(
            @PathVariable Long restaurantId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        RestaurantResponseDto response =
                restaurantService.getRestaurantById(
                        restaurantId,
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant retrieved successfully",
                        response
                )
        );
    }

    @PutMapping("/{restaurantId}")
    public ResponseEntity<ApiResponse<RestaurantResponseDto>>
    updateRestaurant(
            @PathVariable Long restaurantId,
            @Valid @RequestBody RestaurantRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        RestaurantResponseDto response =
                restaurantService.updateRestaurant(
                        restaurantId,
                        request,
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant updated successfully",
                        response
                )
        );
    }

    @PatchMapping("/{restaurantId}/status")
    public ResponseEntity<ApiResponse<RestaurantResponseDto>>
    updateRestaurantStatus(
            @PathVariable Long restaurantId,
            @Valid @RequestBody UpdateRestaurantStatusRequestDto request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        RestaurantResponseDto response =
                restaurantService.updateRestaurantStatus(
                        restaurantId,
                        request.status(),
                        userDetails.getUsername()
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant status updated successfully",
                        response
                )
        );
    }
}
