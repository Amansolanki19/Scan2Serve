package com.example.scantoserve.service;

import com.example.scantoserve.dto.LoginResponseDto;
import com.example.scantoserve.dto.PasswordResetRequestDto;
import com.example.scantoserve.dto.SendOtpRequestDto;
import com.example.scantoserve.dto.VerifyOtpRequestDto;

public interface OtpService {
    void sendLoginOtp(SendOtpRequestDto request);

    LoginResponseDto verifyLoginOtp(VerifyOtpRequestDto request);

    void sendPasswordResetOtp(SendOtpRequestDto request);

    void resetPassword(PasswordResetRequestDto request);
}
