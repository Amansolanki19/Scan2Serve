package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.dto.MenuItemRequestDto;
import com.example.scantoserve.dto.MenuItemResponseDto;
import com.example.scantoserve.entity.*;
import com.example.scantoserve.exception.BadRequestException;
import com.example.scantoserve.exception.ResourceNotFoundException;
import com.example.scantoserve.repository.MenuCategoryRepo;
import com.example.scantoserve.repository.MenuItemRepo;
import com.example.scantoserve.repository.RestaurantRepo;
import com.example.scantoserve.repository.UserRepo;
import com.example.scantoserve.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuItemServiceImpl implements MenuItemService {
    private final MenuItemRepo menuItemRepo;
    private final MenuCategoryRepo menuCategoryRepo;
    private final RestaurantRepo restaurantRepo;
    private final UserRepo userRepo;

    @Override
    public MenuItemResponseDto createMenuItem(Long restaurantId, Long categoryId, MenuItemRequestDto request, String userEmail) {
        getAuthorizedRestaurant(restaurantId, userEmail);

        MenuCategory category = getCategoryForRestaurant(
                restaurantId, categoryId
        );

        if (category.getStatus() != MenuCategoryStatus.ACTIVE) {
            throw new BadRequestException(
                    "Cannot add items to an inactive category"
            );
        }

        validateDiscountPrice(request);

        MenuItem menuItem = MenuItem.builder()
                .name(request.name().trim())
                .description(request.description())
                .price(request.price())
                .discountPrice(request.discountPrice())
                .foodType(request.foodType())
                .imageUrl(request.imageUrl())
                .preparationTime(request.preparationTime())
                .status(MenuItemStatus.AVAILABLE)
                .category(category)
                .build();

        MenuItem savedItem = menuItemRepo.save(menuItem);

        return mapToResponse(savedItem);
    }

    @Override
    public List<MenuItemResponseDto> getCategoryItems(Long restaurantId, Long categoryId, String userEmail) {
        getCategoryForRestaurant(restaurantId, categoryId);
        getAuthorizedRestaurant(restaurantId, userEmail);

        return menuItemRepo
                .findAllByCategory_IdOrderByCreatedAtDesc(categoryId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public MenuItemResponseDto getMenuItemById(Long restaurantId, Long categoryId, Long itemId, String userEmail) {
        getAuthorizedRestaurant(restaurantId, userEmail);

        getCategoryForRestaurant(restaurantId, categoryId);

        MenuItem menuItem = menuItemRepo
                .findByIdAndCategory_Id(itemId, categoryId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu item not found with ID: " + itemId
                ));

        return mapToResponse(menuItem);
    }

    private Restaurant getAuthorizedRestaurant(
            Long restaurantId,
            String userEmail) {

        User currentUser = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"
                ));

        if (currentUser.getRole() != UserRole.RESTAURANT_ADMIN
                && currentUser.getRole() != UserRole.SUPER_ADMIN) {

            throw new AccessDeniedException(
                    "You are not authorized to manage menu items"
            );
        }

        Restaurant restaurant = restaurantRepo.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Restaurant not found with ID: " + restaurantId
                ));

        boolean isSuperAdmin =
                currentUser.getRole() == UserRole.SUPER_ADMIN;

        boolean isRestaurantOwner =
                restaurant.getOwner().getId().equals(currentUser.getId());

        if (!isSuperAdmin && !isRestaurantOwner) {
            throw new AccessDeniedException(
                    "You cannot manage menu items for this restaurant"
            );
        }

        return restaurant;
    }

    private MenuCategory getCategoryForRestaurant(
            Long restaurantId,
            Long categoryId) {

        return menuCategoryRepo
                .findByIdAndRestaurant_Id(categoryId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found in this restaurant"
                ));
    }

    private void validateDiscountPrice(MenuItemRequestDto request) {

        if (request.discountPrice() != null
                && request.discountPrice().compareTo(request.price()) >= 0) {

            throw new BadRequestException(
                    "Discount price must be lower than the original price"
            );
        }
    }

    private MenuItemResponseDto mapToResponse(MenuItem menuItem) {

        MenuCategory category = menuItem.getCategory();

        return new MenuItemResponseDto(
                menuItem.getId(),
                menuItem.getName(),
                menuItem.getDescription(),
                menuItem.getPrice(),
                menuItem.getDiscountPrice(),
                menuItem.getFoodType(),
                menuItem.getImageUrl(),
                menuItem.getPreparationTime(),
                menuItem.getStatus(),
                category.getId(),
                category.getName(),
                category.getRestaurant().getId(),
                menuItem.getCreatedAt(),
                menuItem.getUpdatedAt()
        );
    }
}
