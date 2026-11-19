package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.dto.DiningTableRequestDto;
import com.example.scantoserve.dto.DiningTableResponseDto;
import com.example.scantoserve.entity.*;
import com.example.scantoserve.exception.DuplicateResourceException;
import com.example.scantoserve.exception.ResourceNotFoundException;
import com.example.scantoserve.repository.DiningTableRepo;
import com.example.scantoserve.repository.RestaurantRepo;
import com.example.scantoserve.repository.UserRepo;
import com.example.scantoserve.service.DiningTableService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DiningTableServiceImpl implements DiningTableService {

    private final DiningTableRepo diningTableRepo;
    private final RestaurantRepo restaurantRepo;
    private final UserRepo userRepo;

    @Override
    public DiningTableResponseDto createTable(Long restaurantId, DiningTableRequestDto request, String userEmail) {
        Restaurant restaurant = getAuthorizedRestaurant(
                restaurantId, userEmail
        );

        if (diningTableRepo.existsByRestaurant_IdAndTableNumber(
                restaurantId, request.tableNumber())) {

            throw new DuplicateResourceException(
                    "This table number already exists in the restaurant"
            );
        }

        DiningTable diningTable = DiningTable.builder()
                .tableNumber(request.tableNumber())
                .capacity(request.capacity())
                .status(DiningTableStatus.AVAILABLE)
                .qrToken(UUID.randomUUID().toString())
                .restaurant(restaurant)
                .build();

        DiningTable savedTable = diningTableRepo.save(diningTable);

        return mapToResponse(savedTable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiningTableResponseDto> getRestaurantTables(Long restaurantId, String userEmail) {
        getAuthorizedRestaurant(restaurantId, userEmail);

        return diningTableRepo
                .findAllByRestaurant_IdOrderByTableNumberAsc(restaurantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DiningTableResponseDto getTableById(Long restaurantId, Long tableId, String userEmail) {
        getAuthorizedRestaurant(restaurantId, userEmail);

        DiningTable diningTable = diningTableRepo
                .findByIdAndRestaurant_Id(tableId, restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dining table not found with ID: " + tableId
                ));

        return mapToResponse(diningTable);
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
                    "You are not authorized to manage dining tables"
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
                    "You cannot manage tables for this restaurant"
            );
        }

        return restaurant;
    }

    private DiningTableResponseDto mapToResponse(DiningTable diningTable) {

        return new DiningTableResponseDto(
                diningTable.getId(),
                diningTable.getTableNumber(),
                diningTable.getCapacity(),
                diningTable.getStatus(),
                diningTable.getQrToken(),
                diningTable.getRestaurant().getId(),
                diningTable.getCreatedAt()
        );
    }
}
