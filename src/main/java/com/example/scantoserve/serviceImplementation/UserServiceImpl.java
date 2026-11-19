package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.dto.UserRegistrationRequest;
import com.example.scantoserve.dto.UserResponse;
import com.example.scantoserve.exception.DuplicateResourceException;
import com.example.scantoserve.repository.UserRepo;
import com.example.scantoserve.service.UserService;
import com.example.scantoserve.entity.User;
import com.example.scantoserve.entity.UserRole;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepo userRepo;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;


    @Override
    public UserResponse registerUser(UserRegistrationRequest request) {
        if (userRepo.existsByEmail(request.email())) {
            throw  new DuplicateResourceException("Duplicate Email");
        }

        if (userRepo.existsByMobileNumber(request.mobileNumber())) {
            throw  new DuplicateResourceException("Duplicate Mobile Number");
        }

        User registerUser = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .mobileNumber(String.valueOf(request.mobileNumber()))
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .password(bCryptPasswordEncoder.encode(request.password()))
                .enabled(true)
                .role(UserRole.CUSTOMER)
                .build();

        userRepo.save(registerUser);

        return mapToUserResponse(registerUser);

    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepo.findAll()
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        if(userRepo.existsById(id)){
            User user = userRepo.findById(id).orElseThrow(()-> new UsernameNotFoundException("User not found"));
            return mapToUserResponse(user);
        }
        else{
            throw new UsernameNotFoundException("User not found with id: " + id);
        }
    }

    @Override
    public UserResponse getUserByEmail(String email) {
        User user = userRepo.findByEmail(email).orElseThrow(()-> new UsernameNotFoundException("User not found with email: "+email));

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .role(user.getRole())
                .enabled(Boolean.TRUE.equals(user.getEnabled()))
                .build();

        return response;
    }


}
