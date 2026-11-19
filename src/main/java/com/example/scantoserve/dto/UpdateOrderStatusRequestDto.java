package com.example.scantoserve.dto;

import com.example.scantoserve.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequestDto(

        @NotNull(message = "Order status is required")
        OrderStatus orderStatus

) {
}