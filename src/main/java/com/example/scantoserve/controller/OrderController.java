package com.example.scantoserve.controller;

import com.example.scantoserve.common.ApiResponse;
import com.example.scantoserve.dto.*;
import com.example.scantoserve.entity.OrderStatus;
import com.example.scantoserve.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // Get all orders of the logged-in customer
    @GetMapping("/my/page")
    public ResponseEntity<ApiResponse<Page<OrderResponseDto>>> getMyOrdersPaginated(
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        Page<OrderResponseDto> response =
                orderService.getMyOrders(pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Customer orders fetched successfully",
                        response
                )
        );
    }

    // Place an order
    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<OrderResponseDto>> checkout(
            @Valid @RequestBody CheckoutRequestDto request) {

        OrderResponseDto response = orderService.checkout(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Order placed successfully",
                        response
                ));
    }

    // Get order details
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponseDto>> getOrderById(
            @PathVariable Long orderId) {

        OrderResponseDto response = orderService.getOrderById(orderId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Order fetched successfully",
                        response
                )
        );
    }

    // Get all orders of a restaurant
    @GetMapping("/restaurant/{restaurantId}/page")
    public ResponseEntity<ApiResponse<Page<OrderResponseDto>>>getRestaurantOrdersPaginated(
            @PathVariable Long restaurantId,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {

        Page<OrderResponseDto> response =
                orderService.getRestaurantOrders(restaurantId, pageable);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Restaurant orders fetched successfully",
                        response
                )
        );
    }

    // Filter restaurant orders by status
    @GetMapping("/restaurant/{restaurantId}/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponseDto>>> getRestaurantOrdersByStatus(
            @PathVariable Long restaurantId,
            @PathVariable OrderStatus status) {

        List<OrderResponseDto> response =
                orderService.getRestaurantOrdersByStatus(
                        restaurantId,
                        status
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Filtered restaurant orders fetched successfully",
                        response
                )
        );
    }

    // Update order status
    @PatchMapping("/restaurant/{restaurantId}/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponseDto>> updateOrderStatus(
            @PathVariable Long restaurantId,
            @PathVariable Long orderId,
            @Valid @RequestBody UpdateOrderStatusRequestDto request) {

        OrderResponseDto response = orderService.updateOrderStatus(
                restaurantId,
                orderId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Order status updated successfully",
                        response
                )
        );
    }
}