package com.example.scantoserve.service;

import com.example.scantoserve.dto.*;

public interface CartService {

    CartResponseDto addItem(String qrToken, AddToCartRequestDto request);

    CartResponseDto getCart(String qrToken);

    CartResponseDto updateItem(
            String qrToken,
            Long menuItemId,
            UpdateCartItemRequestDto request
    );

    CartResponseDto removeItem(String qrToken, Long menuItemId);

    void clearCart(String qrToken);
}