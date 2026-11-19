package com.example.scantoserve.controller;

import com.example.scantoserve.common.ApiResponse;
import com.example.scantoserve.dto.*;
import com.example.scantoserve.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    // Add an item to cart
    @PostMapping("/{qrToken}/items")
    public ResponseEntity<ApiResponse<CartResponseDto>> addItem(
            @PathVariable String qrToken,
            @Valid @RequestBody AddToCartRequestDto request) {

        CartResponseDto response = cartService.addItem(qrToken, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Item added to cart successfully",
                        response
                ));
    }

    // View cart
    @GetMapping("/{qrToken}")
    public ResponseEntity<ApiResponse<CartResponseDto>> getCart(
            @PathVariable String qrToken) {

        CartResponseDto response = cartService.getCart(qrToken);

        return ResponseEntity.ok(
                ApiResponse.success("Cart fetched successfully", response)
        );
    }

    // Update item quantity
    @PatchMapping("/{qrToken}/items/{menuItemId}")
    public ResponseEntity<ApiResponse<CartResponseDto>> updateItem(
            @PathVariable String qrToken,
            @PathVariable Long menuItemId,
            @Valid @RequestBody UpdateCartItemRequestDto request) {

        CartResponseDto response = cartService.updateItem(
                qrToken,
                menuItemId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cart item quantity updated successfully",
                        response
                )
        );
    }

    // Remove an item
    @DeleteMapping("/{qrToken}/items/{menuItemId}")
    public ResponseEntity<ApiResponse<CartResponseDto>> removeItem(
            @PathVariable String qrToken,
            @PathVariable Long menuItemId) {

        CartResponseDto response = cartService.removeItem(
                qrToken,
                menuItemId
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Item removed from cart successfully",
                        response
                )
        );
    }

    // Clear entire cart
    @DeleteMapping("/{qrToken}")
    public ResponseEntity<ApiResponse<Void>> clearCart(
            @PathVariable String qrToken) {

        cartService.clearCart(qrToken);

        return ResponseEntity.ok(
                ApiResponse.success("Cart cleared successfully", null)
        );
    }
}