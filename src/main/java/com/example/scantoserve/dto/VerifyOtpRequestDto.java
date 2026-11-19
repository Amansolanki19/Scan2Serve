package com.example.scantoserve.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequestDto(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Pattern(
                regexp = "^[0-9]{6}$",
                message = "OTP must be contain 6 digits"
        )
        String otp
) {
}
