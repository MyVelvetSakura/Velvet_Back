package com.velvet.sakura.service;

import com.velvet.sakura.dto.response.AchievementResponse;
import com.velvet.sakura.dto.response.UserProgressResponse;
import com.velvet.sakura.entity.*;
import com.velvet.sakura.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ProgressServiceImpl implements ProgressService {

    private static final int XP_PER_READING = 10;
    private static final int XP_PER_LEVEL = 100;
    private static final int CREDITS_PER_LEVEL_UP = 3;
    public static final int RETRY_COST = 15;

    private final UserProgressRepository progressRepository;
    private final AchievementRepository achievementRepository;
    private final UserAchievementRepository userAchievementRepository;
    private final ReadingRepository readingRepository;

    @Override
    public UserProgressResponse getProgress(Long accountId) {
        UserProgress progress = getOrCreateProgress(accountId);
        int xpToNext = XP_PER_LEVEL - progress.getExperience();
        return new UserProgressResponse(
                progress.getLevel(),
                progress.getExperience(),
                xpToNext,
                progress.getCredits(),
                progress.getTotalReadings()
        );
    }

    @Override
    public List<AchievementResponse> getAchievements(Long accountId) {
        List<Achievement> all = achievementRepository.findAll();
        Set<String> unlockedCodes = new HashSet<>();
        userAchievementRepository.findByAccountId(accountId)
                .forEach(ua -> unlockedCodes.add(ua.getAchievementCode()));

        return all.stream()
                .map(a -> new AchievementResponse(
                        a.getCode(), a.getTitle(), a.getDescription(),
                        a.getCreditsReward(), unlockedCodes.contains(a.getCode())
                ))
                .toList();
    }

    @Override
    public void registerReadingCompleted(Long accountId, String deckType, Long pastCardId, Long presentCardId, Long futureCardId) {
        UserProgress progress = getOrCreateProgress(accountId);

        progress.setTotalReadings(progress.getTotalReadings() + 1);
        addExperience(progress, XP_PER_READING);
        progressRepository.save(progress);

        checkAchievements(accountId, progress, deckType);
    }

    @Override
    public boolean spendCreditsForRetry(Long accountId, int cost) {
        UserProgress progress = getOrCreateProgress(accountId);
        if (progress.getCredits() < cost) {
            return false;
        }
        progress.setCredits(progress.getCredits() - cost);
        progressRepository.save(progress);
        return true;
    }

    private void addExperience(UserProgress progress, int amount) {
        int newXp = progress.getExperience() + amount;
        while (newXp >= XP_PER_LEVEL) {
            newXp -= XP_PER_LEVEL;
            progress.setLevel(progress.getLevel() + 1);
            progress.setCredits(progress.getCredits() + CREDITS_PER_LEVEL_UP);
        }
        progress.setExperience(newXp);
    }

    private void checkAchievements(Long accountId, UserProgress progress, String deckType) {
        unlockIfNeeded(accountId, "first_reading", progress.getTotalReadings() >= 1);
        unlockIfNeeded(accountId, "readings_10", progress.getTotalReadings() >= 10);
        unlockIfNeeded(accountId, "readings_50", progress.getTotalReadings() >= 50);
        unlockIfNeeded(accountId, "readings_100", progress.getTotalReadings() >= 100);

        boolean usedBothDecks = readingRepository.findByUserId(accountId).stream()
                .map(r -> r.getDeckType().toString())
                .distinct()
                .count() >= 2;
        unlockIfNeeded(accountId, "both_decks", usedBothDecks);

        long distinctCardsUsed = readingRepository.findByUserId(accountId).stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getPastCardId(), r.getPresentCardId(), r.getFutureCardId()))
                .distinct()
                .count();
        unlockIfNeeded(accountId, "full_album", distinctCardsUsed >= 55);
    }

    private void unlockIfNeeded(Long accountId, String code, boolean conditionMet) {
        if (!conditionMet) return;
        if (userAchievementRepository.existsByAccountIdAndAchievementCode(accountId, code)) return;

        UserAchievement unlocked = UserAchievement.builder()
                .accountId(accountId)
                .achievementCode(code)
                .unlockedAt(LocalDateTime.now())
                .build();
        userAchievementRepository.save(unlocked);

        achievementRepository.findById(code).ifPresent(achievement -> {
            UserProgress progress = getOrCreateProgress(accountId);
            progress.setCredits(progress.getCredits() + achievement.getCreditsReward());
            progressRepository.save(progress);
        });
    }

    private UserProgress getOrCreateProgress(Long accountId) {
        return progressRepository.findById(accountId)
                .orElseGet(() -> progressRepository.save(
                        UserProgress.builder().accountId(accountId).build()
                ));
    }
}