
package com.example.scantoserve.dto;

import com.example.scantoserve.entity.PaymentMethod;
import com.example.scantoserve.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDto(

        Long paymentId,

        Long orderId,

        PaymentMethod paymentMethod,

        PaymentStatus paymentStatus,

        BigDecimal amount,

        String transactionReference,

        Long recordedBy,

        String recordedByName,

        LocalDateTime paidAt,

        LocalDateTime createdAt

) {
}