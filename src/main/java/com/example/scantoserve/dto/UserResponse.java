package com.example.scantoserve.dto;

public record UserResponse(
        String id,
        String fullName,
        String email,
        String mobileNumber,
        String role,
        boolean enabled
) {
}