package com.example.scantoserve.service;

import com.example.scantoserve.dto.UserRegistrationRequest;
import com.example.scantoserve.dto.UserResponse;

import java.util.List;

public interface UserService {
    UserResponse registerUser(UserRegistrationRequest request);
    List<UserResponse> getAllUsers();
    UserResponse getUserById(Long id);
    UserResponse getUserByEmail(String email);
}
