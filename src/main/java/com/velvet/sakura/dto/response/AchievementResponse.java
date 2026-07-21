package com.velvet.sakura.dto.response;

public record AchievementResponse(
        String code,
        String title,
        String description,
        int creditsReward,
        boolean unlocked
) {}