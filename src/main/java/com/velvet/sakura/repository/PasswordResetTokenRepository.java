package com.velvet.sakura.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.velvet.sakura.entity.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByAccountId(Long accountId);
}