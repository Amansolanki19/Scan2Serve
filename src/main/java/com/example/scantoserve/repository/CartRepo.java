package com.example.scantoserve.repository;

import com.example.scantoserve.entity.Cart;
import com.example.scantoserve.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {

    Optional<Cart> findByCustomer_IdAndDiningTable_IdAndStatus(
            Long customerId,
            Long diningTableId,
            CartStatus status
    );

    Optional<Cart> findByIdAndCustomer_Id(Long cartId, Long customerId);
}