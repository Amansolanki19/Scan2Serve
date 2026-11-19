package com.example.scantoserve.controller;

import com.example.scantoserve.common.ApiResponse;
import com.example.scantoserve.dto.LoginRequestDto;
import com.example.scantoserve.dto.LoginResponseDto;
import com.example.scantoserve.dto.PasswordResetRequestDto;
import com.example.scantoserve.dto.RefreshTokenRequestDto;
import com.example.scantoserve.dto.SendOtpRequestDto;
import com.example.scantoserve.dto.VerifyOtpRequestDto;
import com.example.scantoserve.service.AuthenticationService;
import com.example.scantoserve.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final OtpService otpService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        return ResponseEntity.ok(authenticationService.login(loginRequest));

    }

    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<Void>> sendLoginOtp(
            @Valid @RequestBody SendOtpRequestDto request) {
        otpService.sendLoginOtp(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(
                        "If the account exists, an OTP was sent.",
                        null
                ));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<LoginResponseDto> verifyLoginOtp(
            @Valid @RequestBody VerifyOtpRequestDto request) {
        return ResponseEntity.ok(otpService.verifyLoginOtp(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(
            @Valid @RequestBody RefreshTokenRequestDto request) {
        return ResponseEntity.ok(authenticationService.refresh(request));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody SendOtpRequestDto request) {
        otpService.sendPasswordResetOtp(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(
                        "If the account exists, a password reset code was sent.",
                        null
                ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody PasswordResetRequestDto request) {
        otpService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success(
                "Password reset successfully. Please sign in.",
                null
        ));
    }

}
