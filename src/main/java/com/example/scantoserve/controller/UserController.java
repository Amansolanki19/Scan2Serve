package com.example.scantoserve.controller;

import com.example.scantoserve.configuration.CustomUserDetails;
import com.example.scantoserve.dto.UserRegistrationRequest;
import com.example.scantoserve.dto.UserResponse;
import com.example.scantoserve.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/users", "/api/user"})
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(
            @Valid @RequestBody UserRegistrationRequest request) {

        UserResponse response = userService.registerUser(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable Long id){
        UserResponse userResponse = userService.getUserById(id);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/email")
    public ResponseEntity<UserResponse> getUserByEmail(@RequestParam String email){
        UserResponse userResponse = userService.getUserByEmail(email);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        return ResponseEntity.ok(userService.getUserByEmail(customUserDetails.getUsername()));
    }


}
