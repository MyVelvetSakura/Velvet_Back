package com.velvet.sakura.dto.response;

public record UserProgressResponse(
        int level,
        int experience,
        int experienceToNextLevel,
        int credits,
        int totalReadings
) {}