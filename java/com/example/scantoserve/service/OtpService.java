package com.example.scantoserve.service;

import com.example.scantoserve.dto.LoginResponseDto;
import com.example.scantoserve.dto.SendOtpRequestDto;
import com.example.scantoserve.dto.VerifyOtpRequestDto;

public interface OtpService {
    void sendLoginOtp(SendOtpRequestDto request);

    LoginResponseDto verifyLoginOtp(VerifyOtpRequestDto request);
}
