package com.example.scantoserve.repository;

import com.example.scantoserve.entity.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepo extends JpaRepository<RefreshToken, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select token from RefreshToken token where token.tokenHash = :tokenHash")
    Optional<RefreshToken> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update RefreshToken token
            set token.revoked = true
            where token.user.id = :userId and token.revoked = false
            """)
    int revokeActiveByUserId(@Param("userId") Long userId);
}
