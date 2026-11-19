package com.example.scantoserve.service;

import com.example.scantoserve.dto.*;
import com.example.scantoserve.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface OrderService {

    OrderResponseDto checkout(CheckoutRequestDto request);

    OrderResponseDto getOrderById(Long orderId);

    List<OrderResponseDto> getMyOrders();

    Page<OrderResponseDto> getMyOrders(Pageable pageable);

    Page<OrderResponseDto> getRestaurantOrders(
            Long restaurantId,
            Pageable pageable
    );

    List<OrderResponseDto> getRestaurantOrders(Long restaurantId);

    List<OrderResponseDto> getRestaurantOrdersByStatus(
            Long restaurantId,
            OrderStatus status
    );

    OrderResponseDto updateOrderStatus(
            Long restaurantId,
            Long orderId,
            UpdateOrderStatusRequestDto request
    );

}