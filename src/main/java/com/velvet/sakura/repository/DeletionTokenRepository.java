package com.velvet.sakura.repository;

import com.velvet.sakura.entity.DeletionToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DeletionTokenRepository extends JpaRepository<DeletionToken, Long> {
    Optional<DeletionToken> findByToken(String token);
    void deleteByAccountId(Long accountId);
}