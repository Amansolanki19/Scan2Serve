package com.example.scantoserve.repository;

import com.example.scantoserve.entity.Payment;
import com.example.scantoserve.entity.PaymentMethod;
import com.example.scantoserve.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {

    Optional<Payment> findByCustomerOrder_Id(Long orderId);

    boolean existsByCustomerOrder_Id(Long orderId);


    // Count successful payments by payment method
    long countByCustomerOrder_Restaurant_IdAndPaymentMethodAndPaymentStatus(
            Long restaurantId,
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus
    );

    // Calculate successful payment amount by payment method
    @Query("""
       SELECT COALESCE(SUM(p.amount), 0)
       FROM Payment p
       WHERE p.customerOrder.restaurant.id = :restaurantId
       AND p.paymentMethod = :paymentMethod
       AND p.paymentStatus = :paymentStatus
       """)
    BigDecimal calculateAmountByPaymentMethod(
            @Param("restaurantId") Long restaurantId,
            @Param("paymentMethod") PaymentMethod paymentMethod,
            @Param("paymentStatus") PaymentStatus paymentStatus
    );


    List<Payment> findAllByCustomerOrder_Restaurant_IdAndPaymentStatusAndPaidAtGreaterThanEqualAndPaidAtLessThanOrderByPaidAtAsc(
            Long restaurantId, PaymentStatus paymentStatus, LocalDateTime start, LocalDateTime end);



}