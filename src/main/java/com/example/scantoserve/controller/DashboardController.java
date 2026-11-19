
package com.example.scantoserve.controller;

import com.example.scantoserve.common.ApiResponse;
import com.example.scantoserve.dto.*;
import com.example.scantoserve.entity.OrderStatus;
import com.example.scantoserve.service.DashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/restaurants/{restaurantId}/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // Dashboard overview
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<DashboardOverviewResponseDto>>
    getDashboardOverview(@PathVariable Long restaurantId) {

        DashboardOverviewResponseDto response =
                dashboardService.getDashboardOverview(restaurantId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Dashboard overview fetched successfully",
                        response
                )
        );
    }

    // Revenue summary
    @GetMapping("/revenue")
    public ResponseEntity<ApiResponse<RevenueSummaryResponseDto>>
    getRevenueSummary(@PathVariable Long restaurantId) {

        RevenueSummaryResponseDto response =
                dashboardService.getRevenueSummary(restaurantId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Revenue summary fetched successfully",
                        response
                )
        );
    }

    // Order status summary
    @GetMapping("/orders/status-summary")
    public ResponseEntity<ApiResponse<OrderStatusSummaryResponseDto>>
    getOrderStatusSummary(@PathVariable Long restaurantId) {

        OrderStatusSummaryResponseDto response =
                dashboardService.getOrderStatusSummary(restaurantId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Order status summary fetched successfully",
                        response
                )
        );
    }


    @GetMapping("/payments/method-summary")
    public ResponseEntity<ApiResponse<List<PaymentMethodSummaryResponseDto>>>
    getPaymentMethodSummary(@PathVariable Long restaurantId) {

        List<PaymentMethodSummaryResponseDto> response =
                dashboardService.getPaymentMethodSummary(restaurantId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Payment method summary fetched successfully",
                        response
                )
        );
    }


    @GetMapping("/revenue/daily")
    public ResponseEntity<ApiResponse<List<DailyRevenueResponseDto>>> getDailyRevenue(
            @PathVariable Long restaurantId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {

        List<DailyRevenueResponseDto> response =
                dashboardService.getDailyRevenue(
                        restaurantId,
                        startDate,
                        endDate
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Daily revenue fetched successfully",
                        response
                )
        );
    }


    @GetMapping("/orders/filter")
    public ResponseEntity<ApiResponse<OrderPageResponse<OrderResponseDto>>>
    filterOrders(
            @PathVariable Long restaurantId,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            @RequestParam(required = false)
            OrderStatus orderStatus,

            @RequestParam(defaultValue = "0") int page,

            @RequestParam(defaultValue = "10") int size) {

        OrderFilterRequestDto request = new OrderFilterRequestDto(
                startDate,
                endDate,
                orderStatus
        );

        OrderPageResponse<OrderResponseDto> response =
                dashboardService.filterOrders(
                        restaurantId,
                        request,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Orders filtered successfully",
                        response
                )
        );
    }


    @GetMapping("/menu-table-summary")
    public ResponseEntity<ApiResponse<MenuAndTableSummaryResponseDto>>
    getMenuAndTableSummary(
            @PathVariable Long restaurantId) {

        MenuAndTableSummaryResponseDto response =
                dashboardService.getMenuAndTableSummary(restaurantId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Menu and table statistics fetched successfully",
                        response
                )
        );
    }




}
