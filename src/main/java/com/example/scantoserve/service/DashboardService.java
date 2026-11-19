
package com.example.scantoserve.service;

import com.example.scantoserve.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface DashboardService {

    DashboardOverviewResponseDto getDashboardOverview(Long restaurantId);

    RevenueSummaryResponseDto getRevenueSummary(Long restaurantId);

    OrderStatusSummaryResponseDto getOrderStatusSummary(Long restaurantId);

    List<PaymentMethodSummaryResponseDto> getPaymentMethodSummary(Long restaurantId);

    List<DailyRevenueResponseDto> getDailyRevenue(Long restaurantId, LocalDate startDate, LocalDate endDate);

    OrderPageResponse<OrderResponseDto> filterOrders(Long restaurantId, OrderFilterRequestDto request, int page, int size);

    MenuAndTableSummaryResponseDto getMenuAndTableSummary(Long restaurantId);
}
