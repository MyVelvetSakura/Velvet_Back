package com.velvet.sakura.service;

import com.velvet.sakura.dto.response.AchievementResponse;
import com.velvet.sakura.dto.response.UserProgressResponse;
import java.util.List;

public interface ProgressService {
    UserProgressResponse getProgress(Long accountId);
    List<AchievementResponse> getAchievements(Long accountId);
    void registerReadingCompleted(Long accountId, String deckType, Long pastCardId, Long presentCardId, Long futureCardId);
    boolean spendCreditsForRetry(Long accountId, int cost);
}