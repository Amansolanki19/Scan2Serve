package com.example.scantoserve.service;

import com.example.scantoserve.dto.LoginRequestDto;
import com.example.scantoserve.dto.LoginResponseDto;

public interface AuthenticationService {
    LoginResponseDto login(LoginRequestDto loginRequest);
}
