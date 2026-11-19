package com.example.scantoserve.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendOtpRequestDto(
        @NotBlank
        @Email
        String email
) {
}
