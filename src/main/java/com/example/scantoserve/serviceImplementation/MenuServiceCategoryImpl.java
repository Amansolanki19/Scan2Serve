package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.dto.MenuCategoryRequestDto;
import com.example.scantoserve.dto.MenuCategoryResponseDto;
import com.example.scantoserve.entity.*;
import com.example.scantoserve.exception.DuplicateResourceException;
import com.example.scantoserve.exception.ResourceNotFoundException;
import com.example.scantoserve.repository.MenuCategoryRepo;
import com.example.scantoserve.repository.RestaurantRepo;
import com.example.scantoserve.repository.UserRepo;
import com.example.scantoserve.service.MenuCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MenuServiceCategoryImpl implements MenuCategoryService {

    private final MenuCategoryRepo menuCategoryRepo;
    private final RestaurantRepo restaurantRepo;
    private final UserRepo userRepo;

    @Override
    public MenuCategoryResponseDto createCategory(Long restaurantId, MenuCategoryRequestDto request, String userEmail) {
        Restaurant restaurant = getAuthorizedRestaurant(
                restaurantId, userEmail
        );

        String categoryName = request.name().trim();

        if (menuCategoryRepo.existsByRestaurant_IdAndNameIgnoreCase(
                restaurantId, categoryName)) {

            throw new DuplicateResourceException(
                    "This category already exists in the restaurant"
            );
        }
        MenuCategory category = MenuCategory.builder()
                .name(categoryName)
                .description(request.description())
                .displayOrder(
                        request.displayOrder() != null
                                ? request.displayOrder()
                                : 0
                )
                .status(MenuCategoryStatus.ACTIVE)
                .restaurant(restaurant)
                .build();

        MenuCategory savedCategory = menuCategoryRepo.save(category);

        return mapToResponse(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MenuCategoryResponseDto> getRestaurantCategories(Long restaurantId, String userEmail) {
        getAuthorizedRestaurant(restaurantId, userEmail);

        return menuCategoryRepo
                .findAllByRestaurant_IdOrderByDisplayOrderAsc(restaurantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MenuCategoryResponseDto getCategoryById(Long restaurantId, Long categoryId, String userEmail) {
        getAuthorizedRestaurant(restaurantId, userEmail);

        MenuCategory category = menuCategoryRepo
                .findByIdAndRestaurant_Id(categoryId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Menu category not found with ID: " + categoryId
                ));

        return mapToResponse(category);
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
                    "You are not authorized to manage menu categories"
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
                    "You cannot manage categories for this restaurant"
            );
        }

        return restaurant;
    }

    private MenuCategoryResponseDto mapToResponse(MenuCategory category) {

        return new MenuCategoryResponseDto(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getStatus(),
                category.getDisplayOrder(),
                category.getRestaurant().getId(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

}
