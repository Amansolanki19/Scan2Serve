
package com.example.scantoserve.dto;

public record MenuAndTableSummaryResponseDto(

        Long totalCategories,
        Long activeCategories,
        Long inactiveCategories,

        Long totalMenuItems,
        Long availableItems,
        Long unavailableItems,

        Long totalTables,
        Long availableTables,
        Long occupiedTables,
        Long inactiveTables

) {}
