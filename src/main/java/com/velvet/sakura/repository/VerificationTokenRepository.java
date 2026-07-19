package com.velvet.sakura.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.velvet.sakura.entity.VerificationToken;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);
    void deleteByAccountId(Long accountId);
}
