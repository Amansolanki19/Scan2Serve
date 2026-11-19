package com.example.scantoserve.serviceImplementation;

import com.example.scantoserve.dto.LoginResponseDto;
import com.example.scantoserve.dto.SendOtpRequestDto;
import com.example.scantoserve.dto.VerifyOtpRequestDto;
import com.example.scantoserve.entity.OtpPurpose;
import com.example.scantoserve.entity.OtpVerification;
import com.example.scantoserve.exception.InvalidOtpException;
import com.example.scantoserve.repository.OtpVerificationRepo;
import com.example.scantoserve.repository.UserRepo;
import com.example.scantoserve.service.EmailService;
import com.example.scantoserve.service.JwtService;
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
    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    private final SecureRandom secureRandom =  new SecureRandom();

    @Value("${otp.expiration-minutes:5}")
    private long expirationMinutes;

    @Value("${otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Value("${jwt.expiration}")
    private long jwtExpiration;


    @Override
    @Transactional
    public void sendLoginOtp(SendOtpRequestDto request) {

        String email = normalizeEmail(request.email());

        if(!userRepo.existsByEmail(email)){return;}

        LocalDateTime now = LocalDateTime.now();
        var existingOtp = otpVerificationRepo.findTopByEmailAndPurposeOrderByCreatedAtDesc(
                email,
                OtpPurpose.LOGIN
        );

        if(existingOtp.isPresent()){
            OtpVerification previous = existingOtp.get();

            if (previous.getCreatedAt().plusSeconds(
                    resendCooldownSeconds
            ).isAfter(now)) {
                return;
            }
        }

        otpVerificationRepo.deleteByEmailAndPurpose(
                email, OtpPurpose.LOGIN
        );

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        OtpVerification verification = OtpVerification.builder()
                .email(email)
                .otpHash(passwordEncoder.encode(otp))
                .purpose(OtpPurpose.LOGIN)
                .expiresAt(now.plusMinutes(expirationMinutes))
                .attempts(0)
                .verified(false)
                .consumed(false)
                .createdAt(now)
                .build();

        otpVerificationRepo.save(verification);

        emailService.sendOtpEmail(email, otp, expirationMinutes);

    }

    @Override
    @Transactional(noRollbackFor = InvalidOtpException.class)
    public LoginResponseDto verifyLoginOtp(VerifyOtpRequestDto request) {

        String email = normalizeEmail(request.email());

        OtpVerification verification = otpVerificationRepo
                .findTopByEmailAndPurposeOrderByCreatedAtDesc(
                        email, OtpPurpose.LOGIN
                )
                .orElseThrow(() ->
                        new InvalidOtpException("Invalid or expired OTP")
                );

        LocalDateTime now = LocalDateTime.now();

        if (verification.isConsumed()
                || verification.isVerified()
                || !verification.getExpiresAt().isAfter(now)) {

            throw new InvalidOtpException("Invalid or expired OTP");
        }

        if (verification.getAttempts() >= maxAttempts) {
            verification.setConsumed(true);
            otpVerificationRepo.save(verification);

            throw new InvalidOtpException("Maximum OTP attempts exceeded");
        }

        if (!passwordEncoder.matches(
                request.otp(), verification.getOtpHash())) {

            verification.setAttempts(
                    verification.getAttempts() + 1
            );

            if (verification.getAttempts() >= maxAttempts) {
                verification.setConsumed(true);
            }

            otpVerificationRepo.save(verification);

            throw new InvalidOtpException("Invalid or expired OTP");
        }

        verification.setVerified(true);
        verification.setConsumed(true);

        otpVerificationRepo.save(verification);

        UserDetails userDetails =
                customUserDetailsService.loadUserByUsername(email);

        String accessToken = jwtService.generateToken(userDetails);

        return new LoginResponseDto(
                accessToken,
                "Bearer",
                jwtExpiration
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
