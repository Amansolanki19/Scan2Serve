package com.example.scantoserve.service;

import com.example.scantoserve.dto.RestaurantRequestDto;
import com.example.scantoserve.dto.RestaurantResponseDto;
import com.example.scantoserve.entity.RestaurantStatus;

import java.nio.file.AccessDeniedException;
import java.util.List;

public interface RestaurantService {
    RestaurantResponseDto createRestaurant(RestaurantRequestDto restaurantRequestDto,
                                           String email) throws AccessDeniedException;


    List<RestaurantResponseDto> getMyRestaurants(
            String ownerEmail
    );

    RestaurantResponseDto getRestaurantById(
            Long restaurantId,
            String currentUserEmail
    );

    RestaurantResponseDto updateRestaurant(
            Long restaurantId,
            RestaurantRequestDto request,
            String currentUserEmail
    );

    RestaurantResponseDto updateRestaurantStatus(
            Long restaurantId,
            RestaurantStatus status,
            String currentUserEmail
    );
}
