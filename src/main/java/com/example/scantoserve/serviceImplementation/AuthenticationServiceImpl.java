package com.example.scantoserve.serviceImplementation;


import com.example.scantoserve.dto.LoginRequestDto;
import com.example.scantoserve.dto.LoginResponseDto;
import com.example.scantoserve.dto.RefreshTokenRequestDto;
import com.example.scantoserve.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    @Override
    public LoginResponseDto login(LoginRequestDto loginRequest) {
        String email = loginRequest.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        loginRequest.password()
                )
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return refreshTokenService.issue(userDetails);
    }

    @Override
    public LoginResponseDto refresh(RefreshTokenRequestDto request) {
        return refreshTokenService.refresh(request.refreshToken());
    }
}
