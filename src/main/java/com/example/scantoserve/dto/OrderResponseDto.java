package com.example.scantoserve.dto;

import com.example.scantoserve.entity.OrderStatus;
import com.example.scantoserve.entity.PaymentStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record OrderResponseDto(

        Long orderId,

        Long customerId,

        String customerName,

        Long restaurantId,

        String restaurantName,

        Long diningTableId,

        Integer tableNumber,

        OrderStatus orderStatus,

        PaymentStatus paymentStatus,

        List<OrderItemResponseDto> items,

        Integer totalItems,

        BigDecimal subtotal,

        BigDecimal taxAmount,

        BigDecimal discountAmount,

        BigDecimal totalAmount,

        LocalDateTime createdAt,

        LocalDateTime updatedAt

) {
}