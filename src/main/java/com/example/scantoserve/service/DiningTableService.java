package com.example.scantoserve.service;

import com.example.scantoserve.dto.DiningTableRequestDto;
import com.example.scantoserve.dto.DiningTableResponseDto;

import java.util.List;

public interface DiningTableService {
    DiningTableResponseDto createTable(
            Long restaurantId,
            DiningTableRequestDto request,
            String userEmail
    );

    List<DiningTableResponseDto> getRestaurantTables(
            Long restaurantId,
            String userEmail
    );

    DiningTableResponseDto getTableById(
            Long restaurantId,
            Long tableId,
            String userEmail
    );
}
