package com.example.scantoserve.repository;

import com.example.scantoserve.entity.MenuCategory;
import com.example.scantoserve.entity.MenuCategoryStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuCategoryRepo
        extends JpaRepository<MenuCategory, Long> {

    List<MenuCategory> findAllByRestaurant_IdOrderByDisplayOrderAsc(
            Long restaurantId
    );

    List<MenuCategory>
    findAllByRestaurant_IdAndStatusOrderByDisplayOrderAsc(
            Long restaurantId,
            MenuCategoryStatus status
    );

    Optional<MenuCategory> findByIdAndRestaurant_Id(
            Long categoryId,
            Long restaurantId
    );

    boolean existsByRestaurant_IdAndNameIgnoreCase(
            Long restaurantId,
            String name
    );

    long countByRestaurant_Id(Long restaurantId);

    long countByRestaurant_IdAndStatus(
            Long restaurantId,
            MenuCategoryStatus status
    );
}