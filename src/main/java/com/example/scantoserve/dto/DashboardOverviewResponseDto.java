
package com.example.scantoserve.dto;

import java.math.BigDecimal;

public record DashboardOverviewResponseDto(

        Long totalOrders,

        Long todayOrders,

        Long pendingOrders,

        Long completedOrders,

        Long cancelledOrders,

        BigDecimal totalRevenue,

        BigDecimal todayRevenue,

        Long totalCustomers,

        Long totalMenuItems,

        Long totalDiningTables

) {}
