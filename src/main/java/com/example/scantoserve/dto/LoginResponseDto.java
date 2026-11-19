package com.example.scantoserve.dto;

public record LoginResponseDto(
        String accessToken,
        String tokenType,
        long expiresIn
) {

}
