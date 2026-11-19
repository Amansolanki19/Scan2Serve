package com.example.scantoserve.service;


public interface EmailService {
    void sendOtpEmail(String email, String otp, long expirationMinutes);
}
