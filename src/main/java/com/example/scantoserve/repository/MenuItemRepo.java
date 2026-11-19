package com.example.scantoserve.repository;

import com.example.scantoserve.entity.MenuItem;
import com.example.scantoserve.entity.MenuItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepo extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findAllByCategory_IdOrderByCreatedAtDesc(
            Long categoryId
    );

    Optional<MenuItem> findByIdAndCategory_Id(
            Long itemId,
            Long categoryId
    );

    List<MenuItem> findAllByCategory_IdAndStatusOrderByCreatedAtDesc(
            Long categoryId,
            MenuItemStatus status
    );

    long countByCategory_Restaurant_Id(Long restaurantId);

    long countByCategory_Restaurant_IdAndStatus(Long restaurantId, MenuItemStatus status);



}
