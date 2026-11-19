package com.example.scantoserve.repository;

import com.example.scantoserve.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Optional;

@Repository
public interface RestaurantRepo extends JpaRepository<Restaurant, Long> {
    List<Restaurant> findAllByOwner_IdOrderByCreatedAtDesc(Long ownerId);
    Optional<Restaurant> findByIdAndOwner_Id(Long id, Long ownerId);
    Boolean existsByOwner_IdAndNameIgnoreCase(Long ownerId, String name);

    Optional<Restaurant> findByOwner_Email(String email);
}
