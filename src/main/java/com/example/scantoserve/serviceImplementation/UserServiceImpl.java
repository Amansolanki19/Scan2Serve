package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.dto.UserRegistrationRequest;
import com.example.scantoserve.dto.UserResponse;
import com.example.scantoserve.repository.UserRepo;
import com.example.scantoserve.service.UserService;
import com.example.scantoserve.user.User;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserResponse registerUser(UserRegistrationRequest request) {

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        String mobileNumber = request.mobileNumber().trim();

        // Check duplicate email
        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email is already registered"
            );
        }

        // Check duplicate mobile number
        if (userRepo.existsByPhone(mobileNumber)) {
            throw new IllegalArgumentException(
                    "Mobile number is already registered"
            );
        }

        // Create User document
        User user = User.builder()
                .fullName(request.fullName().trim())
                .email(email)
                .mobileNumber(mobileNumber)
                .password(passwordEncoder.encode(request.password()))
                .role("CUSTOMER")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        // Save user in MongoDB
        User savedUser = userRepo.save(user);

        // Return safe response
        return new UserResponse(
                savedUser.getId(),
                savedUser.getFullName(),
                savedUser.getEmail(),
                savedUser.getMobileNumber(),
                savedUser.getRole(),
                savedUser.isEnabled()
        );
    }
    @Override
    public List<UserResponse> getAllUsers() {
        return userRepo.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(String id) {
        User user = userRepo.findById(id).orElseThrow(()-> new IllegalArgumentException("User not found"));

        return mapToResponse(user);
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepo.findByEmail(email).orElseThrow(()-> new IllegalArgumentException("User not found"));

        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getMobileNumber(),
                user.getRole(),
                user.isEnabled()
        );
    }
}
