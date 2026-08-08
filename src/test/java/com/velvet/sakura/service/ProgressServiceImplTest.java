package com.velvet.sakura.service;

import com.velvet.sakura.dto.response.UserProgressResponse;
import com.velvet.sakura.entity.Achievement;
import com.velvet.sakura.entity.Reading;
import com.velvet.sakura.entity.UserProgress;
import com.velvet.sakura.repository.AchievementRepository;
import com.velvet.sakura.repository.ReadingRepository;
import com.velvet.sakura.repository.UserAchievementRepository;
import com.velvet.sakura.repository.UserProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProgressServiceImplTest {

    @Mock private UserProgressRepository progressRepository;
    @Mock private AchievementRepository achievementRepository;
    @Mock private UserAchievementRepository userAchievementRepository;
    @Mock private ReadingRepository readingRepository;

    @InjectMocks
    private ProgressServiceImpl progressService;

    private UserProgress progress;

    @BeforeEach
    void setUp() {
        progress = UserProgress.builder()
                .accountId(1L)
                .level(1)
                .experience(0)
                .credits(0)
                .totalReadings(0)
                .build();

        lenient().when(userAchievementRepository.existsByAccountIdAndAchievementCode(anyLong(), anyString()))
                .thenReturn(false);
    }

    @Test
    void getProgress_devuelveElProgresoConXpRestanteParaSubirDeNivel() {
        progress.setLevel(2);
        progress.setExperience(30);
        progress.setCredits(15);
        progress.setTotalReadings(5);

        when(progressRepository.findById(1L)).thenReturn(java.util.Optional.of(progress));

        UserProgressResponse response = progressService.getProgress(1L);

        assertThat(response.level()).isEqualTo(2);
        assertThat(response.experience()).isEqualTo(30);
        assertThat(response.experienceToNextLevel()).isEqualTo(70); // 100 - 30
        assertThat(response.credits()).isEqualTo(15);
        assertThat(response.totalReadings()).isEqualTo(5);
    }

    @Test
    void registerReadingCompleted_sumaXpSinLlegarAlSiguienteNivel() {
        progress.setExperience(50);

        when(progressRepository.findById(1L)).thenReturn(java.util.Optional.of(progress));
        when(readingRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(achievementRepository.findAll()).thenReturn(Collections.emptyList());

        progressService.registerReadingCompleted(1L, "SAKURA", 1L, 2L, 3L);

        assertThat(progress.getExperience()).isEqualTo(60); // 50 + 10
        assertThat(progress.getLevel()).isEqualTo(1);
        assertThat(progress.getTotalReadings()).isEqualTo(1);
        assertThat(progress.getCredits()).isZero();
        verify(progressRepository, atLeastOnce()).save(progress);
    }

    @Test
    void registerReadingCompleted_alSuperarLos100XpSubeDeNivelYOtorgaCreditos() {
        progress.setExperience(95);

        when(progressRepository.findById(1L)).thenReturn(java.util.Optional.of(progress));
        when(readingRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(achievementRepository.findAll()).thenReturn(Collections.emptyList());

        progressService.registerReadingCompleted(1L, "SAKURA", 1L, 2L, 3L);

        assertThat(progress.getLevel()).isEqualTo(2);
        assertThat(progress.getExperience()).isEqualTo(5);
        assertThat(progress.getCredits()).isEqualTo(3);
    }


    @Test
    void spendCreditsForRetry_conCreditosSuficientes_descuentaYDevuelveTrue() {
        progress.setCredits(20);

        when(progressRepository.findById(1L)).thenReturn(java.util.Optional.of(progress));

        boolean result = progressService.spendCreditsForRetry(1L, 15);

        assertThat(result).isTrue();
        assertThat(progress.getCredits()).isEqualTo(5);
        verify(progressRepository).save(progress);
    }

    @Test
    void spendCreditsForRetry_sinCreditosSuficientes_noDescuentaYDevuelveFalse() {
        progress.setCredits(10);

        when(progressRepository.findById(1L)).thenReturn(java.util.Optional.of(progress));

        boolean result = progressService.spendCreditsForRetry(1L, 15);

        assertThat(result).isFalse();
        assertThat(progress.getCredits()).isEqualTo(10); // no cambia
        verify(progressRepository, never()).save(any());
    }

    @Test
    void registerReadingCompleted_conPrimeraLectura_desbloqueaLogroFirstReading() {
        Achievement firstReading = Achievement.builder()
                .code("first_reading")
                .title("Primera tirada")
                .description("desc")
                .creditsReward(5)
                .build();

        when(progressRepository.findById(1L)).thenReturn(java.util.Optional.of(progress));
        when(readingRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(achievementRepository.findAll()).thenReturn(List.of(firstReading));
        when(achievementRepository.findById("first_reading")).thenReturn(java.util.Optional.of(firstReading));

        progressService.registerReadingCompleted(1L, "SAKURA", 1L, 2L, 3L);

        verify(userAchievementRepository).save(argThat(ua -> ua.getAchievementCode().equals("first_reading")));
        assertThat(progress.getCredits()).isEqualTo(5); // recompensa del logro
    }

    @Test
    void registerReadingCompleted_conLogroYaDesbloqueado_noLoVuelveADesbloquear() {
        when(progressRepository.findById(1L)).thenReturn(java.util.Optional.of(progress));
        when(readingRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        when(achievementRepository.findAll()).thenReturn(Collections.emptyList());
        when(userAchievementRepository.existsByAccountIdAndAchievementCode(1L, "first_reading"))
                .thenReturn(true);
        progressService.registerReadingCompleted(1L, "SAKURA", 1L, 2L, 3L);

        verify(userAchievementRepository, never()).save(any());
    }

   @Test
void registerReadingCompleted_conAmbosMazosUsados_desbloqueaLogroBothDecks() {
    Achievement bothDecks = Achievement.builder()
            .code("both_decks")
            .title("Dualidad")
            .description("desc")
            .creditsReward(15)
            .build();

    Reading oldReadingClow = Reading.builder()
            .id(1L).userId(1L).deckType(com.velvet.sakura.entity.DeckType.CLOW)
            .pastCardId(10L).presentCardId(11L).futureCardId(12L)
            .build();

    Reading newReadingSakura = Reading.builder()
            .id(2L).userId(1L).deckType(com.velvet.sakura.entity.DeckType.SAKURA)
            .pastCardId(1L).presentCardId(2L).futureCardId(3L)
            .build();

    when(progressRepository.findById(1L)).thenReturn(java.util.Optional.of(progress));
    when(readingRepository.findByUserId(1L)).thenReturn(List.of(oldReadingClow, newReadingSakura));
    when(achievementRepository.findAll()).thenReturn(List.of(bothDecks));
    when(achievementRepository.findById("both_decks")).thenReturn(java.util.Optional.of(bothDecks));

    progressService.registerReadingCompleted(1L, "SAKURA", 1L, 2L, 3L);

    verify(userAchievementRepository).save(argThat(ua -> ua.getAchievementCode().equals("both_decks")));
}
}