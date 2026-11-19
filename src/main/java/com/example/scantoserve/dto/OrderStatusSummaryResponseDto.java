
package com.example.scantoserve.dto;

public record OrderStatusSummaryResponseDto(

        Long placedOrders,

        Long confirmedOrders,

        Long preparingOrders,

        Long readyOrders,

        Long servedOrders,

        Long completedOrders,

        Long cancelledOrders

) {}
