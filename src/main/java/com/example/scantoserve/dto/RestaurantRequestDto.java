package com.example.scantoserve.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RestaurantRequestDto(
        @NotBlank(message = "Restaurant name is required")
        @Size(max = 150)
        String name,

        @Size(max = 1000)
        String description,

        @NotBlank(message = "Restaurant email is required")
        @Email(message = "Invalid restaurant email")
        String email,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^\\+?[0-9]{10,15}$",
                message = "Invalid phone number"
        )
        String phoneNumber,

        @NotBlank(message = "Address is required")
        @Size(max = 255)
        String addressLine,

        @NotBlank(message = "City is required")
        @Size(max = 100)
        String city,

        @NotBlank(message = "State is required")
        @Size(max = 100)
        String state,

        @NotBlank(message = "Postal code is required")
        @Size(min = 3, max = 10)
        String postalCode,

        @NotBlank(message = "Country is required")
        @Size(max = 100)
        String country

) {
}
