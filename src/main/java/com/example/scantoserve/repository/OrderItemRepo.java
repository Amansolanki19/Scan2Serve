package com.example.scantoserve.repository;

import com.example.scantoserve.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepo extends JpaRepository<OrderItem, Long> {

    // Find all items belonging to an order
    List<OrderItem> findAllByCustomerOrder_Id(Long orderId);

    // Delete all items belonging to an order
    void deleteAllByCustomerOrder_Id(Long orderId);
}