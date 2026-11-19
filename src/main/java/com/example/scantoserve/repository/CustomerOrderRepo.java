package com.example.scantoserve.repository;

import com.example.scantoserve.entity.CustomerOrder;
import com.example.scantoserve.entity.OrderStatus;
import com.example.scantoserve.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepo
        extends JpaRepository<CustomerOrder, Long> {

    List<CustomerOrder> findAllByCustomer_IdOrderByCreatedAtDesc(
            Long customerId
    );

    Page<CustomerOrder> findAllByCustomer_IdOrderByCreatedAtDesc(
            Long customerId,
            Pageable pageable
    );

    List<CustomerOrder> findAllByRestaurant_IdOrderByCreatedAtDesc(
            Long restaurantId
    );

    Page<CustomerOrder> findAllByRestaurant_IdOrderByCreatedAtDesc(
            Long restaurantId,
            Pageable pageable
    );

    List<CustomerOrder> findAllByRestaurant_IdAndOrderStatusOrderByCreatedAtDesc(
            Long restaurantId,
            OrderStatus orderStatus
    );

    Page<CustomerOrder>
    findAllByRestaurant_IdAndOrderStatusOrderByCreatedAtDesc(
            Long restaurantId,
            OrderStatus orderStatus,
            Pageable pageable
    );

    Optional<CustomerOrder> findByIdAndCustomer_Id(
            Long orderId,
            Long customerId
    );

    Optional<CustomerOrder> findByIdAndRestaurant_Id(
            Long orderId,
            Long restaurantId
    );


    Page<CustomerOrder>
    findAllByRestaurant_IdAndCreatedAtBetweenOrderByCreatedAtDesc(Long restaurantId, LocalDateTime start,
            LocalDateTime end,
            Pageable pageable);

    Page<CustomerOrder> findAllByRestaurant_IdAndOrderStatusAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long restaurantId, OrderStatus orderStatus, LocalDateTime start, LocalDateTime end, Pageable pageable);



    // Total orders for a restaurant
    long countByRestaurant_Id(Long restaurantId);

    // Orders filtered by status
    long countByRestaurant_IdAndOrderStatus(
            Long restaurantId,
            OrderStatus orderStatus
    );

    // Orders created within a date range
    long countByRestaurant_IdAndCreatedAtBetween(
            Long restaurantId,
            LocalDateTime start,
            LocalDateTime end
    );

    // Successful payment revenue
    @Query("""
       SELECT COALESCE(SUM(o.totalAmount), 0)
       FROM CustomerOrder o
       WHERE o.restaurant.id = :restaurantId
       AND o.paymentStatus = :paymentStatus
       """)
    BigDecimal calculateRevenue(
            @Param("restaurantId") Long restaurantId,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );

    // Revenue within a date range
    @Query("""
       SELECT COALESCE(SUM(o.totalAmount), 0)
       FROM CustomerOrder o
       WHERE o.restaurant.id = :restaurantId
       AND o.paymentStatus = :paymentStatus
       AND o.createdAt BETWEEN :start AND :end
       """)
    BigDecimal calculateRevenueBetween(
            @Param("restaurantId") Long restaurantId,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // Unique customers who placed orders
    @Query("""
       SELECT COUNT(DISTINCT o.customer.id)
       FROM CustomerOrder o
       WHERE o.restaurant.id = :restaurantId
       """)
    long countUniqueCustomers(
            @Param("restaurantId") Long restaurantId
    );

    long countByRestaurant_IdAndPaymentStatus(
            Long restaurantId,
            PaymentStatus paymentStatus
    );

}