
package com.example.scantoserve.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyRevenueResponseDto(
        LocalDate date,
        BigDecimal revenue,
        Long totalPayments
) {}
