
package com.example.scantoserve.dto;

import java.math.BigDecimal;

public record RevenueSummaryResponseDto(

        BigDecimal todayRevenue,

        BigDecimal weeklyRevenue,

        BigDecimal monthlyRevenue,

        Long totalSuccessfulPayments

) {}
