package com.example.scantoserve.service;

import com.example.scantoserve.dto.LoginRequestDto;
import com.example.scantoserve.dto.LoginResponseDto;
import com.example.scantoserve.dto.RefreshTokenRequestDto;

public interface AuthenticationService {
    LoginResponseDto login(LoginRequestDto loginRequest);

    LoginResponseDto refresh(RefreshTokenRequestDto request);
}
