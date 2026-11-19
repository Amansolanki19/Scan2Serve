package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.dto.*;
import com.example.scantoserve.entity.*;
import com.example.scantoserve.exception.ResourceNotFoundException;
import com.example.scantoserve.repository.DiningTableRepo;
import com.example.scantoserve.repository.MenuCategoryRepo;
import com.example.scantoserve.repository.MenuItemRepo;
import com.example.scantoserve.service.PublicMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicMenuServiceImpl implements PublicMenuService {

    private final DiningTableRepo diningTableRepo;
    private final MenuCategoryRepo menuCategoryRepo;
    private final MenuItemRepo menuItemRepo;

    @Override
    public PublicMenuResponseDto getPublicMenu(String qrToken) {

        // 1. Find dining table using QR token
        DiningTable diningTable = diningTableRepo
                .findByQrToken(qrToken)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Invalid QR code or dining table not found"
                ));

        // 2. Get associated restaurant
        Restaurant restaurant = diningTable.getRestaurant();

        // 3. Check restaurant status
        if (restaurant.getStatus() != RestaurantStatus.ACTIVE) {
            throw new ResourceNotFoundException(
                    "Restaurant is currently unavailable"
            );
        }

        // 4. Check dining table status
        if (diningTable.getStatus() == DiningTableStatus.INACTIVE) {
            throw new ResourceNotFoundException(
                    "This dining table is currently unavailable"
            );
        }

        List<MenuCategory> categories =
                menuCategoryRepo
                        .findAllByRestaurant_IdAndStatusOrderByDisplayOrderAsc(
                                restaurant.getId(),
                                MenuCategoryStatus.ACTIVE
                        );

        List<PublicMenuCategoryResponseDto> categoryResponses =
                categories.stream()
                        .map(category -> {

                            List<PublicMenuItemResponseDto> items =
                                    menuItemRepo
                                            .findAllByCategory_IdAndStatusOrderByCreatedAtDesc(
                                                    category.getId(),
                                                    MenuItemStatus.AVAILABLE
                                            )
                                            .stream()
                                            .map(this::mapToItemResponse)
                                            .toList();

                            return new PublicMenuCategoryResponseDto(
                                    category.getId(),
                                    category.getName(),
                                    category.getDescription(),
                                    category.getDisplayOrder(),
                                    items
                            );
                        })
                        .toList();

        // 7. Return complete public menu
        return new PublicMenuResponseDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDescription(),
                diningTable.getId(),
                diningTable.getTableNumber(),
                diningTable.getCapacity(),
                categoryResponses
        );
    }

    private PublicMenuItemResponseDto mapToItemResponse(MenuItem item) {

        return new PublicMenuItemResponseDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getDiscountPrice(),
                item.getFoodType(),
                item.getImageUrl(),
                item.getPreparationTime()
        );
    }
}