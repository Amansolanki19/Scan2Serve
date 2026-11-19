package com.example.scantoserve.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckoutRequestDto(

        @NotBlank(message = "QR token is required")
        String qrToken

) {
}