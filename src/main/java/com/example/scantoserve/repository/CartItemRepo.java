package com.example.scantoserve.repository;

import com.example.scantoserve.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Long> {

    List<CartItem> findAllByCart_Id(Long cartId);

    Optional<CartItem> findByCart_IdAndMenuItem_Id(
            Long cartId,
            Long menuItemId
    );

    Optional<CartItem> findByIdAndCart_Id(
            Long cartItemId,
            Long cartId
    );

    void deleteAllByCart_Id(Long cartId);
}