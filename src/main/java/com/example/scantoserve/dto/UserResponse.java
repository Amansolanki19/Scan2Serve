package com.example.scantoserve.dto;


import com.example.scantoserve.entity.UserRole;
import lombok.Builder;


@Builder
public record UserResponse(
        Long id,
        String fullName,
        String email,
        String mobileNumber,
        UserRole role,
        boolean enabled
) {
}