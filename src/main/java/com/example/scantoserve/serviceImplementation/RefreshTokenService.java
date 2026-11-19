package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.dto.LoginResponseDto;
import com.example.scantoserve.entity.RefreshToken;
import com.example.scantoserve.entity.User;
import com.example.scantoserve.exception.InvalidRefreshTokenException;
import com.example.scantoserve.repository.RefreshTokenRepo;
import com.example.scantoserve.repository.UserRepo;
import com.example.scantoserve.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepo refreshTokenRepo;
    private final UserRepo userRepo;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${jwt.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-expiration:2592000000}")
    private long refreshTokenExpiration;

    @Transactional
    public LoginResponseDto issue(UserDetails userDetails) {
        String refreshToken = createToken(userDetails.getUsername());
        String accessToken = jwtService.generateToken(userDetails);

        return new LoginResponseDto(
                accessToken,
                "Bearer",
                accessTokenExpiration,
                refreshToken,
                refreshTokenExpiration
        );
    }

    @Transactional
    public LoginResponseDto refresh(String presentedToken) {
        if (presentedToken == null || presentedToken.isBlank()) {
            throw new InvalidRefreshTokenException();
        }

        RefreshToken storedToken = refreshTokenRepo
                .findByTokenHashForUpdate(hashToken(presentedToken))
                .orElseThrow(InvalidRefreshTokenException::new);
        LocalDateTime now = LocalDateTime.now();

        if (storedToken.isRevoked()
                || !storedToken.getExpiresAt().isAfter(now)
                || !Boolean.TRUE.equals(storedToken.getUser().getEnabled())) {
            throw new InvalidRefreshTokenException();
        }

        storedToken.setRevoked(true);
        UserDetails userDetails =
                userDetailsService.loadUserByUsername(storedToken.getUser().getEmail());

        return issue(userDetails);
    }

    @Transactional
    public void revokeAllForUser(Long userId) {
        refreshTokenRepo.revokeActiveByUserId(userId);
    }

    private String createToken(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
        LocalDateTime now = LocalDateTime.now();

        refreshTokenRepo.save(RefreshToken.builder()
                .user(user)
                .tokenHash(hashToken(token))
                .createdAt(now)
                .expiresAt(now.plusNanos(refreshTokenExpiration * 1_000_000))
                .revoked(false)
                .build());

        return token;
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 is unavailable", ex);
        }
    }
}
