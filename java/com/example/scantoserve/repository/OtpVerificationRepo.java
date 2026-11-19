package com.example.scantoserve.repository;

import com.example.scantoserve.entity.OtpPurpose;
import com.example.scantoserve.entity.OtpVerification;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface OtpVerificationRepo extends JpaRepository<OtpVerification,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<OtpVerification> findTopByEmailAndPurposeOrderByCreatedAtDesc(
            String email,
            OtpPurpose purpose
    );

    void deleteByEmailAndPurpose(String email,
                                 OtpPurpose purpose);
}
