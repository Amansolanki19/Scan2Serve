
package com.example.scantoserve.dto;

import com.example.scantoserve.entity.PaymentMethod;

import java.math.BigDecimal;

public record PaymentMethodSummaryResponseDto(

        PaymentMethod paymentMethod,

        Long totalPayments,

        BigDecimal totalAmount

) {}
