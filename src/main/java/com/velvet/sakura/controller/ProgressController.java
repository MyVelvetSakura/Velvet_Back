package com.velvet.sakura.controller;

import com.velvet.sakura.dto.response.AchievementResponse;
import com.velvet.sakura.dto.response.UserProgressResponse;
import com.velvet.sakura.service.ProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/{accountId}")
    public UserProgressResponse getProgress(@PathVariable Long accountId) {
        return progressService.getProgress(accountId);
    }

    @GetMapping("/{accountId}/achievements")
    public List<AchievementResponse> getAchievements(@PathVariable Long accountId) {
        return progressService.getAchievements(accountId);
    }

    @PostMapping("/{accountId}/spend-retry")
    public boolean spendForRetry(@PathVariable Long accountId) {
        return progressService.spendCreditsForRetry(accountId, com.velvet.sakura.service.ProgressServiceImpl.RETRY_COST);
    }
}