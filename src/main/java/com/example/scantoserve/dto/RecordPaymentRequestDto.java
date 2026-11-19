package com.example.scantoserve.dto;

import com.example.scantoserve.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecordPaymentRequestDto(
        @NotNull(message = "Payment method is required")
        PaymentMethod paymentMethod,

        @Size(max = 100, message = "Transaction reference cannot exceed 100 characters")
        String transactionReference
) {
}
