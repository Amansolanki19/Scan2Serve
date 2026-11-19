package com.example.scantoserve.repository;

import com.example.scantoserve.entity.DiningTable;
import com.example.scantoserve.entity.DiningTableStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiningTableRepo
        extends JpaRepository<DiningTable, Long> {

    List<DiningTable> findAllByRestaurant_IdOrderByTableNumberAsc(
            Long restaurantId
    );

    Optional<DiningTable> findByIdAndRestaurant_Id(
            Long tableId,
            Long restaurantId
    );

    Optional<DiningTable> findByQrToken(String qrToken);

    boolean existsByRestaurant_IdAndTableNumber(
            Long restaurantId,
            Integer tableNumber
    );

    long countByRestaurant_Id(Long restaurantId);

    long countByRestaurant_IdAndStatus(Long restaurantId, DiningTableStatus status);


}