package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.dto.*;
import com.example.scantoserve.entity.*;
import com.example.scantoserve.exception.BadRequestException;
import com.example.scantoserve.exception.ResourceNotFoundException;
import com.example.scantoserve.repository.*;
import com.example.scantoserve.service.CartService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final CartRepo cartRepo;
    private final CartItemRepo cartItemRepo;
    private final DiningTableRepo diningTableRepo;
    private final UserRepo userRepo;
    private final MenuItemRepo menuItemRepo;

    // Get the currently logged-in customer
    private User getCurrentCustomer() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new BadRequestException("Customer authentication is required");
        }

        return userRepo.findByEmail(userDetails.getUsername())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Customer not found"));
    }

    // Validate QR token and retrieve the dining table
    private DiningTable getValidTable(String qrToken) {

        DiningTable table = diningTableRepo.findByQrToken(qrToken)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Invalid QR code"));

        if (table.getStatus() == DiningTableStatus.INACTIVE) {
            throw new BadRequestException("This dining table is inactive");
        }

        if (table.getRestaurant().getStatus() != RestaurantStatus.ACTIVE) {
            throw new BadRequestException("Restaurant is currently inactive");
        }

        return table;
    }

    // Find an existing active cart or create one
    private Cart getOrCreateCart(User customer, DiningTable table) {

        return cartRepo
                .findByCustomer_IdAndDiningTable_IdAndStatus(
                        customer.getId(),
                        table.getId(),
                        CartStatus.ACTIVE
                )
                .orElseGet(() -> cartRepo.save(
                        Cart.builder()
                                .customer(customer)
                                .diningTable(table)
                                .restaurant(table.getRestaurant())
                                .status(CartStatus.ACTIVE)
                                .build()
                ));
    }

    // Find an active cart without creating a new one
    private Cart getExistingCart(User customer, DiningTable table) {

        return cartRepo
                .findByCustomer_IdAndDiningTable_IdAndStatus(
                        customer.getId(),
                        table.getId(),
                        CartStatus.ACTIVE
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException("Active cart not found"));
    }

    // Validate that the menu item belongs to this restaurant
    private MenuItem getAvailableMenuItem(Long menuItemId, Long restaurantId) {

        MenuItem item = menuItemRepo.findById(menuItemId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Menu item not found"));

        if (!item.getCategory().getRestaurant().getId().equals(restaurantId)) {
            throw new BadRequestException(
                    "Menu item does not belong to this restaurant");
        }

        if (item.getCategory().getStatus() != MenuCategoryStatus.ACTIVE) {
            throw new BadRequestException("Menu category is inactive");
        }

        if (item.getStatus() != MenuItemStatus.AVAILABLE) {
            throw new BadRequestException("Menu item is unavailable");
        }

        return item;
    }

    // Use discounted price when available
    private BigDecimal getEffectivePrice(MenuItem item) {

        if (item.getDiscountPrice() != null) {
            return item.getDiscountPrice();
        }

        return item.getPrice();
    }

    @Override
    public CartResponseDto addItem(
            String qrToken,
            AddToCartRequestDto request
    ) {

        User customer = getCurrentCustomer();
        DiningTable table = getValidTable(qrToken);

        Cart cart = getOrCreateCart(customer, table);

        MenuItem menuItem = getAvailableMenuItem(
                request.menuItemId(),
                table.getRestaurant().getId()
        );

        CartItem cartItem = cartItemRepo
                .findByCart_IdAndMenuItem_Id(
                        cart.getId(),
                        menuItem.getId()
                )
                .orElse(null);

        if (cartItem == null) {

            cartItem = CartItem.builder()
                    .cart(cart)
                    .menuItem(menuItem)
                    .quantity(request.quantity())
                    .unitPrice(getEffectivePrice(menuItem))
                    .build();

        } else {

            cartItem.setQuantity(
                    cartItem.getQuantity() + request.quantity()
            );

        }

        cartItemRepo.save(cartItem);

        return mapToCartResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponseDto getCart(String qrToken) {

        User customer = getCurrentCustomer();
        DiningTable table = getValidTable(qrToken);

        Cart cart = getExistingCart(customer, table);

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponseDto updateItem(
            String qrToken,
            Long menuItemId,
            UpdateCartItemRequestDto request
    ) {

        User customer = getCurrentCustomer();
        DiningTable table = getValidTable(qrToken);

        Cart cart = getExistingCart(customer, table);

        CartItem cartItem = cartItemRepo
                .findByCart_IdAndMenuItem_Id(
                        cart.getId(),
                        menuItemId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Item not found in cart"));

        cartItem.setQuantity(request.quantity());

        cartItemRepo.save(cartItem);

        return mapToCartResponse(cart);
    }

    @Override
    public CartResponseDto removeItem(
            String qrToken,
            Long menuItemId
    ) {

        User customer = getCurrentCustomer();
        DiningTable table = getValidTable(qrToken);

        Cart cart = getExistingCart(customer, table);

        CartItem cartItem = cartItemRepo
                .findByCart_IdAndMenuItem_Id(
                        cart.getId(),
                        menuItemId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Item not found in cart"));

        cartItemRepo.delete(cartItem);

        return mapToCartResponse(cart);
    }

    @Override
    public void clearCart(String qrToken) {

        User customer = getCurrentCustomer();
        DiningTable table = getValidTable(qrToken);

        Cart cart = getExistingCart(customer, table);

        cartItemRepo.deleteAllByCart_Id(cart.getId());
    }

    // Convert Cart entity into CartResponse DTO
    private CartResponseDto mapToCartResponse(Cart cart) {

        List<CartItem> cartItems =
                cartItemRepo.findAllByCart_Id(cart.getId());

        List<CartItemResponseDto> itemResponses = cartItems.stream()
                .map(item -> new CartItemResponseDto(
                        item.getId(),
                        item.getMenuItem().getId(),
                        item.getMenuItem().getName(),
                        item.getMenuItem().getDescription(),
                        item.getMenuItem().getImageUrl(),
                        item.getUnitPrice(),
                        item.getQuantity(),
                        item.getSubtotal()
                ))
                .toList();

        BigDecimal totalAmount = itemResponses.stream()
                .map(CartItemResponseDto::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalItems = itemResponses.stream()
                .mapToInt(CartItemResponseDto::quantity)
                .sum();

        return new CartResponseDto(
                cart.getId(),
                cart.getRestaurant().getId(),
                cart.getRestaurant().getName(),
                cart.getDiningTable().getId(),
                cart.getDiningTable().getTableNumber(),
                itemResponses,
                totalItems,
                totalAmount,
                cart.getCreatedAt(),
                cart.getUpdatedAt()
        );
    }
}