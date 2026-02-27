package com.theacademyyouneed.the_academy_you_need_backend.repository;

import com.theacademyyouneed.the_academy_you_need_backend.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    // Clean up expired tokens (call this in a scheduled job later)
    @Modifying
    @Query("DELETE FROM VerificationToken t WHERE t.expiresAt < :now OR t.used = true")
    void deleteExpiredAndUsedTokens(LocalDateTime now);
}