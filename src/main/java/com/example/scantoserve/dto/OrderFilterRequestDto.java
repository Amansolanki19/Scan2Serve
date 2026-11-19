
package com.example.scantoserve.dto;

import com.example.scantoserve.entity.OrderStatus;
import java.time.LocalDate;

public record OrderFilterRequestDto(
        LocalDate startDate,
        LocalDate endDate,
        OrderStatus orderStatus
) {}
