package com.velvet.sakura.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.velvet.sakura.dto.request.GenerateInterpretationRequest;
import com.velvet.sakura.entity.Card;
import com.velvet.sakura.entity.DeckType;
import com.velvet.sakura.repository.CardRepository;
import com.velvet.sakura.security.CustomUserDetailsService;
import com.velvet.sakura.security.JwtService;
import com.velvet.sakura.service.OpenAIService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InterpretationController.class)
@AutoConfigureMockMvc(addFilters = false)
class InterpretationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OpenAIService openAIService;

    @MockitoBean
    private CardRepository cardRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private Card pastCard() {
        return Card.builder()
                .id(1L).code("viento").spanishName("Viento")
                .meaning("Simboliza el intelecto y la sabiduría")
                .deckType(DeckType.SAKURA)
                .build();
    }

    private Card presentCard() {
        return Card.builder()
                .id(2L).code("brillo").spanishName("Brillo")
                .meaning("Simboliza la iluminación")
                .deckType(DeckType.SAKURA)
                .build();
    }

    private Card futureCard() {
        return Card.builder()
                .id(3L).code("flote").spanishName("Flote")
                .meaning("Simboliza una visión general")
                .deckType(DeckType.SAKURA)
                .build();
    }

    @Test
    void generate_conPregunta_devuelve200ConLaInterpretacion() throws Exception {
        GenerateInterpretationRequest request = new GenerateInterpretationRequest();
        request.setQuestion("¿Cómo será mi semana?");
        request.setPastCardId(1L);
        request.setPresentCardId(2L);
        request.setFutureCardId(3L);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(pastCard()));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(presentCard()));
        when(cardRepository.findById(3L)).thenReturn(Optional.of(futureCard()));
        when(openAIService.generateInterpretation(
                eq("¿Cómo será mi semana?"),
                eq("Viento"), eq("Simboliza el intelecto y la sabiduría"),
                eq("Brillo"), eq("Simboliza la iluminación"),
                eq("Flote"), eq("Simboliza una visión general")
        )).thenReturn("Una interpretación de prueba conectada a tu pregunta.");

        mockMvc.perform(post("/api/interpretation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interpretation").value("Una interpretación de prueba conectada a tu pregunta."));
    }

    @Test
    void generate_sinPregunta_igualmenteDevuelve200ConInterpretacionLibre() throws Exception {
        GenerateInterpretationRequest request = new GenerateInterpretationRequest();
        request.setQuestion("");
        request.setPastCardId(1L);
        request.setPresentCardId(2L);
        request.setFutureCardId(3L);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(pastCard()));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(presentCard()));
        when(cardRepository.findById(3L)).thenReturn(Optional.of(futureCard()));
        when(openAIService.generateInterpretation(
                eq(""), anyString(), anyString(), anyString(), anyString(), anyString(), anyString()
        )).thenReturn("Una lectura libre de lo que dicen las cartas.");

        mockMvc.perform(post("/api/interpretation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.interpretation").value("Una lectura libre de lo que dicen las cartas."));
    }

    @Test
    void generate_conCartaDePasadoInexistente_devuelve404() throws Exception {
        GenerateInterpretationRequest request = new GenerateInterpretationRequest();
        request.setQuestion("¿Pregunta?");
        request.setPastCardId(999L);
        request.setPresentCardId(2L);
        request.setFutureCardId(3L);

        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/interpretation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void generate_sinIdDeCartaDePresente_devuelve400PorValidacion() throws Exception {
        GenerateInterpretationRequest request = new GenerateInterpretationRequest();
        request.setQuestion("¿Pregunta?");
        request.setPastCardId(1L);
        request.setPresentCardId(null); // @NotNull falla
        request.setFutureCardId(3L);

        mockMvc.perform(post("/api/interpretation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}