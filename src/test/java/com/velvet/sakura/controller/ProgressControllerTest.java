package com.velvet.sakura.controller;

import com.velvet.sakura.dto.response.AchievementResponse;
import com.velvet.sakura.dto.response.UserProgressResponse;
import com.velvet.sakura.security.CustomUserDetailsService;
import com.velvet.sakura.security.JwtService;
import com.velvet.sakura.service.ProgressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProgressController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProgressService progressService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;


    @Test
    void getProgress_devuelveElProgresoDelUsuario() throws Exception {
        UserProgressResponse response = new UserProgressResponse(2, 30, 70, 15, 5);

        when(progressService.getProgress(10L)).thenReturn(response);

        mockMvc.perform(get("/api/progress/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(2))
                .andExpect(jsonPath("$.experience").value(30))
                .andExpect(jsonPath("$.experienceToNextLevel").value(70))
                .andExpect(jsonPath("$.credits").value(15))
                .andExpect(jsonPath("$.totalReadings").value(5));
    }


    @Test
    void getAchievements_devuelveLaListaCompletaConEstadoDeDesbloqueo() throws Exception {
        AchievementResponse unlocked = new AchievementResponse(
                "first_reading", "Primera tirada", "Completa tu primera lectura.", 5, true
        );
        AchievementResponse locked = new AchievementResponse(
                "readings_50", "Maestra del tarot", "Guarda 50 lecturas.", 30, false
        );

        when(progressService.getAchievements(10L)).thenReturn(List.of(unlocked, locked));

        mockMvc.perform(get("/api/progress/10/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].code").value("first_reading"))
                .andExpect(jsonPath("$[0].unlocked").value(true))
                .andExpect(jsonPath("$[1].code").value("readings_50"))
                .andExpect(jsonPath("$[1].unlocked").value(false));
    }

    @Test
    void getAchievements_sinLogrosDesbloqueados_devuelveTodosConUnlockedFalse() throws Exception {
        AchievementResponse locked = new AchievementResponse(
                "first_reading", "Primera tirada", "Completa tu primera lectura.", 5, false
        );

        when(progressService.getAchievements(20L)).thenReturn(List.of(locked));

        mockMvc.perform(get("/api/progress/20/achievements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unlocked").value(false));
    }


    @Test
    void spendForRetry_conCreditosSuficientes_devuelveTrue() throws Exception {
        when(progressService.spendCreditsForRetry(10L, 15)).thenReturn(true);

        mockMvc.perform(post("/api/progress/10/spend-retry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    void spendForRetry_sinCreditosSuficientes_devuelveFalse() throws Exception {
        when(progressService.spendCreditsForRetry(10L, 15)).thenReturn(false);

        mockMvc.perform(post("/api/progress/10/spend-retry"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
}