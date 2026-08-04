package com.velvet.sakura.repository;

import com.velvet.sakura.entity.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO user_progress (account_id, level, experience, credits, total_readings) " +
                   "VALUES (:accountId, 1, 0, 0, 0) ON CONFLICT (account_id) DO NOTHING",
           nativeQuery = true)
    void insertIfNotExists(@Param("accountId") Long accountId);
}