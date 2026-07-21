package com.velvet.sakura.repository;

import com.velvet.sakura.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByAccountId(Long accountId);
    boolean existsByAccountIdAndAchievementCode(Long accountId, String code);
}