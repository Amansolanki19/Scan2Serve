package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.dto.RestaurantRequestDto;
import com.example.scantoserve.dto.RestaurantResponseDto;
import com.example.scantoserve.entity.Restaurant;
import com.example.scantoserve.entity.RestaurantStatus;
import com.example.scantoserve.entity.User;
import com.example.scantoserve.entity.UserRole;
import com.example.scantoserve.exception.DuplicateResourceException;
import com.example.scantoserve.exception.ResourceNotFoundException;
import com.example.scantoserve.repository.RestaurantRepo;
import com.example.scantoserve.repository.UserRepo;
import com.example.scantoserve.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {
    private final RestaurantRepo restaurantRepo;
    private final UserRepo userRepo;

    @Override
    public RestaurantResponseDto createRestaurant(RestaurantRequestDto request,String email) throws AccessDeniedException {
        User owner =  userRepo.findByEmail(email).orElseThrow(()->new UsernameNotFoundException(
                "User not found with email: " + email
        ));

        if(owner.getRole() != UserRole.RESTAURANT_ADMIN &&
            owner.getRole() != UserRole.SUPER_ADMIN){
            throw new AccessDeniedException("ACCESS DENIED\nOnly restaurant administrators can create restaurants");
        }

        String restaurantName = request.name().trim();

        if(restaurantRepo.existsByOwner_IdAndNameIgnoreCase(owner.getId(), restaurantName)){
            throw new DuplicateResourceException(
                    " \"You already own a restaurant with this name\""
            );
        }

        Restaurant restaurant = Restaurant.builder()
                .name(restaurantName)
                .description(request.description())
                .email(request.email())
                .mobileNumber(request.phoneNumber().trim())
                .addressLine(request.addressLine().trim())
                .city(request.city().trim())
                .state(request.state().trim())
                .postalCode(request.postalCode().trim())
                .country(request.country().trim())
                .owner(owner)
                .status(RestaurantStatus.ACTIVE)
                .build();

        return mapToResponse(restaurantRepo.save(restaurant));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponseDto> getMyRestaurants(String ownerEmail) {
        User owner = findUserByEmail(ownerEmail);

        List<Restaurant> restaurants =
                restaurantRepo.findAllByOwner_IdOrderByCreatedAtDesc(
                        owner.getId()
                );

        return restaurants.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponseDto getRestaurantById(Long restaurantId, String currentUserEmail) {
        Restaurant restaurant = findOwnedRestaurant(
                restaurantId,
                currentUserEmail
        );

        return mapToResponse(restaurant);
    }

    @Override
    public RestaurantResponseDto updateRestaurant(Long restaurantId, RestaurantRequestDto request, String currentUserEmail) {
        Restaurant restaurant = findOwnedRestaurant(
                restaurantId,
                currentUserEmail
        );

        String restaurantName = request.name().trim();

        boolean duplicateName =
                restaurantRepo.existsByOwner_IdAndNameIgnoreCase(
                        restaurant.getOwner().getId(),
                        restaurantName
                );

        if (duplicateName
                && !restaurant.getName().equalsIgnoreCase(restaurantName)) {

            throw new DuplicateResourceException(
                    "You already own a restaurant with this name"
            );
        }

        restaurant.setName(restaurantName);
        restaurant.setDescription(request.description());
        restaurant.setEmail((request.email()));
        restaurant.setMobileNumber(request.phoneNumber().trim());
        restaurant.setAddressLine(request.addressLine().trim());
        restaurant.setCity(request.city().trim());
        restaurant.setState(request.state().trim());
        restaurant.setPostalCode(request.postalCode().trim());
        restaurant.setCountry(request.country().trim());

        return mapToResponse(restaurantRepo.save(restaurant));
    }

    @Override
    public RestaurantResponseDto updateRestaurantStatus(Long restaurantId, RestaurantStatus status, String currentUserEmail) {
        Restaurant restaurant = findOwnedRestaurant(
                restaurantId,
                currentUserEmail
        );

        restaurant.setStatus(status);

        return mapToResponse(restaurantRepo.save(restaurant));
    }

    private User findUserByEmail(String email) {

        return userRepo.findByEmail((email))
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private Restaurant findOwnedRestaurant(
            Long restaurantId,
            String currentUserEmail) {

        User currentUser = findUserByEmail(currentUserEmail);

        if (currentUser.getRole() == UserRole.SUPER_ADMIN) {

            return restaurantRepo.findById(restaurantId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Restaurant not found with id: "
                                            + restaurantId
                            )
                    );
        } return restaurantRepo.findByIdAndOwner_Id(
                        restaurantId,
                        currentUser.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Restaurant not found with id: "
                                        + restaurantId
                        )
                );
    }

    private RestaurantResponseDto mapToResponse(
            Restaurant restaurant) {

        return new RestaurantResponseDto(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDescription(),
                restaurant.getEmail(),
                restaurant.getMobileNumber(),
                restaurant.getAddressLine(),
                restaurant.getCity(),
                restaurant.getState(),
                restaurant.getPostalCode(),
                restaurant.getCountry(),
                restaurant.getStatus(),
                restaurant.getOwner().getId(),
                restaurant.getOwner().getFullName(),
                restaurant.getCreatedAt(),
                restaurant.getUpdatedAt()
        );
    }
}
