package com.example.scantoserve.repository;

import com.example.scantoserve.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Optional;

public interface UserRepo extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);

    @Query("{ 'mobileNumber' : ?0 }")
    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    @Query(value = "{ 'mobileNumber' : ?0 }", exists = true)
    boolean existsByPhone(String phone);
}
