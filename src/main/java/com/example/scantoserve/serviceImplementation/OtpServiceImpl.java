package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.dto.LoginResponseDto;
import com.example.scantoserve.dto.PasswordResetRequestDto;
import com.example.scantoserve.dto.SendOtpRequestDto;
import com.example.scantoserve.dto.VerifyOtpRequestDto;
import com.example.scantoserve.entity.OtpPurpose;
import com.example.scantoserve.entity.OtpVerification;
import com.example.scantoserve.entity.User;
import com.example.scantoserve.exception.InvalidOtpException;
import com.example.scantoserve.repository.OtpVerificationRepo;
import com.example.scantoserve.repository.UserRepo;
import com.example.scantoserve.service.EmailService;
import com.example.scantoserve.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Locale;


@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepo otpVerificationRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;
    private final CustomUserDetailsService customUserDetailsService;

    private final SecureRandom secureRandom =  new SecureRandom();

    @Value("${otp.expiration-minutes:5}")
    private long expirationMinutes;

    @Value("${otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Override
    @Transactional
    public void sendLoginOtp(SendOtpRequestDto request) {
        sendOtp(request.email(), OtpPurpose.LOGIN);
    }

    @Override
    @Transactional
    public void sendPasswordResetOtp(SendOtpRequestDto request) {
        sendOtp(request.email(), OtpPurpose.PASSWORD_RESET);
    }

    @Override
    @Transactional(noRollbackFor = InvalidOtpException.class)
    public LoginResponseDto verifyLoginOtp(VerifyOtpRequestDto request) {
        String email = normalizeEmail(request.email());
        consumeOtp(email, request.otp(), OtpPurpose.LOGIN);

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(email);

        if (!userDetails.isEnabled()) {
            throw new InvalidOtpException("Invalid or expired OTP");
        }

        return refreshTokenService.issue(userDetails);
    }

    @Override
    @Transactional(noRollbackFor = InvalidOtpException.class)
    public void resetPassword(PasswordResetRequestDto request) {
        String email = normalizeEmail(request.email());
        consumeOtp(email, request.otp(), OtpPurpose.PASSWORD_RESET);

        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP"));
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepo.save(user);
        refreshTokenService.revokeAllForUser(user.getId());
    }

    private void sendOtp(String rawEmail, OtpPurpose purpose) {
        String email = normalizeEmail(rawEmail);
        if (!userRepo.existsByEmail(email)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        var existingOtp = otpVerificationRepo
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose);

        if (existingOtp.isPresent()
                && existingOtp.get().getCreatedAt()
                .plusSeconds(resendCooldownSeconds).isAfter(now)) {
            return;
        }

        otpVerificationRepo.deleteByEmailAndPurpose(email, purpose);
        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        OtpVerification verification = OtpVerification.builder()
                .email(email)
                .otpHash(passwordEncoder.encode(otp))
                .purpose(purpose)
                .expiresAt(now.plusMinutes(expirationMinutes))
                .attempts(0)
                .verified(false)
                .consumed(false)
                .createdAt(now)
                .build();

        otpVerificationRepo.save(verification);
        emailService.sendOtpEmail(email, otp, expirationMinutes);
    }

    private void consumeOtp(String email, String otp, OtpPurpose purpose) {
        OtpVerification verification = otpVerificationRepo
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose)
                .orElseThrow(() -> new InvalidOtpException("Invalid or expired OTP"));
        LocalDateTime now = LocalDateTime.now();

        if (verification.isConsumed()
                || verification.isVerified()
                || !verification.getExpiresAt().isAfter(now)
                || verification.getAttempts() >= maxAttempts) {
            verification.setConsumed(true);
            otpVerificationRepo.save(verification);
            throw new InvalidOtpException("Invalid or expired OTP");
        }

        if (!passwordEncoder.matches(otp, verification.getOtpHash())) {
            verification.setAttempts(verification.getAttempts() + 1);
            if (verification.getAttempts() >= maxAttempts) {
                verification.setConsumed(true);
            }
            otpVerificationRepo.save(verification);
            throw new InvalidOtpException("Invalid or expired OTP");
        }

        verification.setVerified(true);
        verification.setConsumed(true);
        otpVerificationRepo.save(verification);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
